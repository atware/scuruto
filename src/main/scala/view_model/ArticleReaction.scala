package view_model

import model.User

case class ArticleReaction(
  stockable: Boolean,
  likeable: Boolean,
  stockedUsers: Seq[User]
)

object ArticleReaction {
  val DISPLAY_LIMIT: Int = 8
}