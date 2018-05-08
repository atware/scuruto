package operation

import model._
import model.typebinder.ArticleId
import org.joda.time.DateTime
import scalikejdbc.DBSession
import skinny.PermittedStrongParameters
import view_model._

/**
 * The operation for articles.
 */
sealed trait ArticleOperation extends OperationBase {
  val PAGE_SIZE: Int = 20
  val POPULARS_MAX: Int = 5
  val RANKING_MAX: Int = 10

  def get(id: ArticleId)(implicit s: DBSession = Article.autoSession): Option[Article]
  def getPage(maxId: ArticleId = ArticleId(Long.MaxValue), pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Seq[Article]
  def getWithTag(id: ArticleId)(implicit s: DBSession = Article.autoSession): Option[Article]
  def getReaction(article: Article, user: User)(implicit s: DBSession = Article.autoSession): ArticleReaction
  def getIndexSidebar(user: User)(implicit s: DBSession = Article.autoSession): IndexSidebar
  def getArticleSidebar(article: Article)(implicit s: DBSession = Article.autoSession): ArticleSidebar

  def getPageByTag(tag: Tag, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article]
  def getPageByUser(user: User, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article]
  def getPageByUserStocked(user: User, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article]

  def create(permittedAttributes: PermittedStrongParameters, tagNames: Seq[String])(implicit s: DBSession = Article.autoSession): Article
  def update(origin: Article, permittedAttributes: PermittedStrongParameters, tagNames: Seq[String])(implicit s: DBSession = Article.autoSession): Article
}

class ArticleOperationImpl extends ArticleOperation {

  override def get(id: ArticleId)(implicit s: DBSession = Article.autoSession): Option[Article] = {
    Article.findById(id)
  }

  override def getPage(maxId: ArticleId = ArticleId(Long.MaxValue), pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Seq[Article] = {
    Article.findPageOrderByDesc(maxId, pageSize)
  }

  override def getWithTag(id: ArticleId)(implicit s: DBSession = Article.autoSession): Option[Article] = {
    Article.joins(Article.tagsRef).findById(id.value)
  }

  override def getReaction(article: Article, user: User)(implicit s: DBSession = Article.autoSession): ArticleReaction = {
    ArticleReaction(
      !Stock.exists(user.userId, article.articleId),
      !Like.exists(user.userId, article.articleId),
      Stock.findByArticleIdOrderByStockIdDesc(article.articleId).flatMap(_.user)
    )
  }

  override def getIndexSidebar(user: User)(implicit s: DBSession = Article.autoSession): IndexSidebar = {
    IndexSidebar(
      User.calcContribution(user.userId),
      Article.findPopulars(POPULARS_MAX),
      User.calcContributionRanking(RANKING_MAX)
    )
  }

  override def getArticleSidebar(article: Article)(implicit s: DBSession = Article.autoSession): ArticleSidebar = {
    ArticleSidebar(
      User.calcContribution(article.author.get.userId)
    )
  }

  override def getPageByTag(tag: Tag, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article] = {
    val totalCount = ArticlesTags.countByTagId(tag.tagId)
    val totalPages = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)
    val articleIds = ArticlesTags.findAllByTagWithPaginationOrderByDesc(tag.tagId, pageNo, pageSize).map { _.articleId }
    val articles = Article.findAllByIdsOrderByDesc(articleIds: _*)
    Pagination(pageNo, totalPages, totalCount, articles)
  }

  override def getPageByUser(user: User, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article] = {
    val totalCount = Article.countByUserId(user.userId)
    val totalPages = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)
    val articles = Article.findAllByUserIdWithPagination(user.userId, pageNo, pageSize)
    Pagination(pageNo, totalPages, totalCount, articles)
  }

  override def getPageByUserStocked(user: User, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = Article.autoSession): Pagination[Article] = {
    val totalCount = Stock.countByUserId(user.userId)
    val totalPages = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)
    val articleIds = Stock.findAllByUserIdWithPaginationOrderByDesc(user.userId, pageNo, pageSize).map { _.articleId }
    val articles = Article.findAllByIdsOrderByStockDesc(user.userId, articleIds: _*)
    Pagination(pageNo, totalPages, totalCount, articles)
  }

  override def create(permittedAttributes: PermittedStrongParameters, tagNames: Seq[String])(implicit s: DBSession = Article.autoSession): Article = {
    // article
    val id = {
      Article.createWithPermittedAttributes(permittedAttributes)
    }
    // tag
    tagNames.foreach { tagName =>
      val tagId = {
        Tag.findByName(tagName) match {
          case Some(tag) => tag.tagId
          case _ => Tag.createWithAttributes('name -> tagName, 'created_at -> DateTime.now)
        }
      }
      ArticlesTags.createWithAttributes('article_id -> id.value, 'tag_id -> tagId.value)
      val counter = ArticlesTags.countByTagId(tagId)
      Tag.updateById(tagId).withAttributes('taggings_count -> counter, 'updated_at -> DateTime.now)
    }

    getWithTag(id).get
  }

  override def update(origin: Article, permittedAttributes: PermittedStrongParameters, tagNames: Seq[String])(implicit s: DBSession = Article.autoSession): Article = {
    val id: ArticleId = origin.articleId

    // article
    Article.updateById(id).withPermittedAttributes(permittedAttributes)

    // history
    val oldTagNames = origin.tags.map { _.name }
    UpdateHistory.createWithAttributes(
      'article_id -> id.value,
      'new_title -> getParameterAsString("title", permittedAttributes),
      'new_tags -> tagNames.mkString(","),
      'new_body -> getParameterAsString("body", permittedAttributes),
      'old_title -> origin.title,
      'old_tags -> oldTagNames.mkString(","),
      'old_body -> origin.body
    )

    // tags
    ArticlesTags.deleteAllByArticleId(id)
    val deleteTags = oldTagNames diff tagNames
    deleteTags.foreach { tagName =>
      val tagId = Tag.findByName(tagName).get.tagId
      val counter = ArticlesTags.countByTagId(tagId)
      Tag.updateById(tagId).withAttributes('taggings_count -> counter, 'updated_at -> DateTime.now)
    }
    tagNames.foreach { tagName =>
      val tagId = {
        Tag.findByName(tagName) match {
          case Some(tag) => tag.tagId
          case _ => Tag.createWithAttributes('name -> tagName, 'created_at -> DateTime.now)
        }
      }
      ArticlesTags.createWithAttributes('article_id -> id.value, 'tag_id -> tagId.value)
      val counter = ArticlesTags.countByTagId(tagId)
      Tag.updateById(tagId).withAttributes('taggings_count -> counter, 'updated_at -> DateTime.now)
    }

    getWithTag(id).get
  }

}
