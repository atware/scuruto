package lib

sealed abstract class NotificationType(val value: String)

object NotificationType {
  case object Comment extends NotificationType("comment")
  case object Reply extends NotificationType("reply")
  case object Stock extends NotificationType("stock")
  case object Like extends NotificationType("like")
}