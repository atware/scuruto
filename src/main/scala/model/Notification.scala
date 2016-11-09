package model

import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

case class Notification(
  notificationId: Long,
  userId: UserId,
  articleId: ArticleId,
  fragmentId: Option[Long],
  senderId: UserId,
  `type`: String,
  state: Boolean,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None
)

object Notification extends SkinnyCRUDMapper[Notification] with TimestampsFeature[Notification] {
  override lazy val tableName = "notifications"
  override lazy val defaultAlias = createAlias("n")
  override lazy val primaryKeyFieldName = "notificationId"

  def findRecentsByUserId(userId: UserId, limit: Int)(implicit s: DBSession = autoSession): Seq[Notification] = {
    Notification.where(sqls.eq(column.userId, userId)).orderBy(column.notificationId.desc).limit(limit).apply()
  }

  def updateStateAsRead(userId: UserId)(implicit s: DBSession = autoSession): Unit = {
    Notification.updateBy(sqls.eq(column.userId, userId)).withAttributes('state -> true)
  }

  override def extract(rs: WrappedResultSet, n: ResultName[Notification]): Notification = autoConstruct(rs, n)
}
