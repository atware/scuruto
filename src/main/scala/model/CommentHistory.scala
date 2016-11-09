package model

import model.typebinder.CommentId
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class CommentHistory(
  commentHistoryId: Long,
  commentId: CommentId,
  newBody: Option[String],
  oldBody: Option[String],
  deleted: Boolean,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None
)

object CommentHistory extends SkinnyCRUDMapper[CommentHistory] with TimestampsFeature[CommentHistory] {
  override lazy val tableName = "comment_histories"
  override lazy val defaultAlias = createAlias("ch")
  override lazy val primaryKeyFieldName = "commentHistoryId"

  override def extract(rs: WrappedResultSet, ch: ResultName[CommentHistory]): CommentHistory = autoConstruct(rs, ch)
}
