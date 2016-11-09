package operation

import model.User._
import model.{ Article, Notification, User }
import scalikejdbc.DBSession
import view_model._

/**
 * The operation for notifications.
 */
sealed trait NotificationOperation extends OperationBase {
  val NOTICE_LIMIT: Int = 15

  def getNotifications(user: User)(implicit s: DBSession = autoSession): NotificationArea
  def updateStateAsRead(user: User)(implicit s: DBSession = autoSession): Unit

}

class NotificationOperationImpl extends NotificationOperation {

  override def getNotifications(user: User)(implicit s: DBSession = autoSession): NotificationArea = {
    val notifications = Notification.findRecentsByUserId(user.userId, NOTICE_LIMIT)
    val count = notifications.count(!_.state)
    val notices = notifications.map { notification =>
      val sender = User.findById(notification.senderId)
      val article = Article.findById(notification.articleId)
      Notice(
        image = sender.flatMap(s => s.imageUrl),
        sender = sender.map(s => s.name).orNull,
        isCommenter = article.map(a => a.userId).getOrElse(-1) != user.userId,
        title = article.map(a => a.title).orNull,
        articleId = notification.articleId.value,
        fragmentId = notification.fragmentId,
        `type` = notification.`type`,
        when = notification.createdAt,
        state = notification.state
      )
    }
    NotificationArea(
      count,
      notices
    )
  }

  override def updateStateAsRead(user: User)(implicit s: DBSession): Unit = {
    Notification.updateStateAsRead(user.userId)
  }

}