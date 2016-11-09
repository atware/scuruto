package model

import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class Comment(
  commentId: CommentId,
  userId: UserId,
  articleId: ArticleId,
  body: String,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  author: Option[User] = None
)

object Comment extends SkinnyCRUDMapperWithId[CommentId, Comment] with TimestampsFeatureWithId[CommentId, Comment] {
  override lazy val tableName = "comments"
  override lazy val defaultAlias = createAlias("c")
  override lazy val primaryKeyFieldName = "commentId"

  belongsToWithFk[User](
    right = User,
    fk = Comment.column.userId,
    merge = (a, u) => a.copy(author = u)
  ).byDefault

  def countByArticleId(articleId: ArticleId)(implicit s: DBSession = autoSession): Long = {
    Comment.countBy(sqls.eq(Comment.column.articleId, articleId))
  }

  def findAllByArticleId(articleId: ArticleId)(implicit s: DBSession = autoSession): Seq[Comment] = {
    Comment.where(sqls.eq(column.articleId, articleId)).orderBy(column.commentId.asc).apply()
  }

  def delete(userId: UserId, commentId: CommentId)(implicit s: DBSession = autoSession): Int = {
    Comment.deleteBy(sqls.eq(column.userId, userId).and.eq(column.commentId, commentId))
  }

  // --------
  def idToRawValue(id: CommentId) = id.value
  def rawValueToId(value: Any) = CommentId(value.toString.toLong)
  override def extract(rs: WrappedResultSet, c: ResultName[Comment]): Comment = autoConstruct(rs, c, "author")
}
