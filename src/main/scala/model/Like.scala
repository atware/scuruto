package model

import model.typebinder.{ ArticleId, LikeId, UserId }
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class Like(
  likeId: LikeId,
  userId: UserId,
  articleId: ArticleId,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  article: Option[Article] = None
)

object Like extends SkinnyCRUDMapperWithId[LikeId, Like] with TimestampsFeatureWithId[LikeId, Like] {
  override lazy val tableName = "likes"
  override lazy val defaultAlias = createAlias("l")
  override lazy val primaryKeyFieldName = "likeId"

  val l = defaultAlias

  val articlesRef = hasOneWithFkAndJoinCondition[Article](
    right = Article,
    fk = Like.column.articleId,
    on = sqls.eq(l.column(column.articleId), Article.a.column(Article.column.articleId)),
    merge = (l, a) => l.copy(article = a)
  )

  def countByArticleId(articleId: ArticleId)(implicit s: DBSession = autoSession): Long = {
    Like.countBy(sqls.eq(column.articleId, articleId))
  }

  def exists(userId: UserId, articleId: ArticleId)(implicit s: DBSession = autoSession): Boolean = {
    Like.countBy(sqls.eq(column.userId, userId).and.eq(column.articleId, articleId)) > 0
  }

  def delete(userId: UserId, articleId: ArticleId)(implicit s: DBSession = autoSession): Unit = {
    Like.deleteBy(sqls.eq(column.userId, userId).and.eq(column.articleId, articleId))
  }

  // --------
  def idToRawValue(id: LikeId) = id.value
  def rawValueToId(value: Any) = LikeId(value.toString.toLong)
  override def extract(rs: WrappedResultSet, l: ResultName[Like]): Like = autoConstruct(rs, l, "article")
}
