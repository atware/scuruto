package view_model

import org.joda.time.DateTime

case class Notice(
  image: Option[String],
  sender: String,
  isCommenter: Boolean,
  title: String,
  articleId: Long,
  fragmentId: Option[Long],
  `type`: String,
  when: Option[DateTime],
  state: Boolean
)

object Notice {}