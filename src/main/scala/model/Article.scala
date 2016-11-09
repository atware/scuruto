package model

import com.github.roundrop.scalikejdbcext.sqlsyntax._
import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.Pagination
import skinny.micro.{ MultiParams, Params }
import skinny.orm._
import skinny.orm.feature._

case class Article(
  articleId: ArticleId,
  userId: UserId,
  title: String,
  body: String,
  commentsCount: Int,
  stocksCount: Int,
  likesCount: Int,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  lockVersion: Option[Long] = Option(0),
  author: Option[User] = None,
  tags: Seq[Tag] = Nil,
  histories: Seq[UpdateHistory] = Nil,
  stock: Option[Stock] = None
)

object Article extends SkinnyCRUDMapperWithId[ArticleId, Article]
    with TimestampsFeatureWithId[ArticleId, Article]
    with OptimisticLockWithVersionFeatureWithId[ArticleId, Article] {

  override lazy val tableName = "articles"
  override lazy val defaultAlias = createAlias("a")
  override lazy val primaryKeyFieldName = "articleId"

  val a = defaultAlias

  belongsToWithFk[User](
    right = User,
    fk = Article.column.userId,
    merge = (a, u) => a.copy(author = u)
  ).byDefault

  val tagsRef = hasManyThroughWithFk[Tag](
    through = ArticlesTags,
    many = Tag,
    throughFk = Article.column.articleId,
    manyFk = ArticlesTags.column.tagId,
    merge = (a, tags) => a.copy(tags = tags)
  )

  val historiesRef = hasMany[UpdateHistory](
    many = UpdateHistory -> UpdateHistory.defaultAlias,
    on = (a, h) => sqls.eq(a.articleId, h.articleId),
    merge = (article, histories) => article.copy(histories = histories)
  )

  val stocksRef = hasOneWithFk[Stock](
    right = Stock,
    fk = column.articleId,
    merge = (article, stock) => article.copy(stock = stock)
  )

  def emptyModel() = Article(ArticleId(-1), UserId(-1), "", "", 0, 0, 0, null, null, null, null, Seq())

  def fromParams(params: Params, multiParams: MultiParams, id: ArticleId = ArticleId(-1)): Article = {
    val title = params.getOrElse("title", "")
    val body = params.getOrElse("body", "")
    val tagNames = multiParams.getOrElse("tags", Seq())
    val tags = {
      if (tagNames.isEmpty) {
        Seq[Tag]()
      } else {
        tagNames.map { tagName =>
          new Tag(TagId(-1), tagName, Some(0), null, null, Nil)
        }.toList
      }
    }
    Article(id, UserId(-1), title, body, 0, 0, 0, null, null, null, null, tags)
  }

  def countByUserId(userId: UserId)(implicit s: DBSession = autoSession): Long = {
    Article.countBy(sqls.eq(a.userId, userId))
  }

  def findPageOrderByDesc(maxId: ArticleId = ArticleId(Long.MaxValue), pageSize: Int)(implicit s: DBSession = autoSession): Seq[Article] = {
    Article.joins(tagsRef).where(sqls.lt(a.articleId, maxId)).orderBy(a.articleId.desc).limit(pageSize).apply()
  }

  def findPopulars(limit: Int)(implicit s: DBSession = autoSession): Seq[Article] = {
    val mdHelpPageId = sys.env.get("MARKDOWN_HELP_PAGE_ID").map(v => ArticleId(v.toLong))
    val ids = Article.findAllByWithLimitOffset(sqls.ne(a.articleId, mdHelpPageId), limit, 0, Seq(a.column(column.stocksCount).desc, a.column(column.articleId).desc))
    Article.joins(tagsRef).where(sqls.in(primaryKeyField, ids.map(a => a.articleId))).orderBy(a.stocksCount.desc, a.articleId.desc).apply()
      .filterNot(_.stocksCount == 0)
  }

  def findAllByUserIdWithPagination(userId: UserId, pageNo: Int, pageSize: Int)(implicit s: DBSession = autoSession): Seq[Article] = {
    Article.joins(tagsRef).where(sqls.eq(a.userId, userId)).paginate(Pagination.page(pageNo).per(pageSize)).orderBy(a.articleId.desc).apply()
  }

  def findAllByIdsOrderByDesc(ids: ArticleId*)(implicit s: DBSession = autoSession): Seq[Article] = {
    Article.joins(tagsRef).findAllBy(
      sqls.in(primaryKeyField, ids.map(idToRawValue)),
      Seq(a.column(column.articleId).desc)
    )
  }

  def findAllByIdsOrderByStockDesc(userId: UserId, ids: ArticleId*)(implicit s: DBSession = autoSession): Seq[Article] = {
    Article.joins(tagsRef).joins(stocksRef).findAllBy(
      sqls.in(primaryKeyField, ids.map(idToRawValue)).
        and.eq(Stock.defaultAlias.column(Stock.column.userId), userId),
      Seq(Stock.defaultAlias.column(Stock.column.stockId).desc)
    )
  }

  def countByTitleOrBody(q: String)(implicit s: DBSession = autoSession): Long = {
    Article.countBy(sqls.likeIgnoreCase(column.title, s"%${q}%").or.likeIgnoreCase(column.body, s"%${q}%"))
  }
  def searchByTitleOrBody(q: String, pageNo: Int, pageSize: Int)(implicit s: DBSession = autoSession): Seq[Article] = {
    Article.joins(tagsRef)
      .where(sqls.toOrConditionOpt(Some(sqls.likeIgnoreCase(column.title, s"%${q}%").or.likeIgnoreCase(column.body, s"%${q}%"))).get)
      .paginate(Pagination.page(pageNo).per(pageSize)).orderBy(a.articleId.desc).apply()
  }

  def findByTagId(tagId: TagId, limit: Int)(implicit s: DBSession = autoSession): Seq[Map[String, Any]] = {
    sql"""
       |SELECT
       |  a.title AS title,
       |  a.article_id AS article_id,
       |  COUNT(s.user_id) AS stockers
       |FROM articles as a
       |LEFT JOIN articles_tags AS at
       |  ON a.article_id = at.article_id
       |LEFT JOIN stocks AS s
       |  ON s.article_id = at.article_id
       |WHERE
       |  at.tag_id = ${tagId.value}
       |GROUP BY a.article_id, a.title
       |ORDER BY stockers DESC, a.article_id DESC
    """.stripMargin
      .map(_.toMap()).list().apply().take(limit)
  }

  // --------
  def idToRawValue(id: ArticleId) = id.value
  def rawValueToId(value: Any) = ArticleId(value.toString.toLong)
  override def extract(rs: WrappedResultSet, a: ResultName[Article]): Article = autoConstruct(rs, a, "author", "tags", "histories", "stock")
}
