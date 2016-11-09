package integration

import model.{ Article, Comment, User }
import skinny.micro.context.SkinnyContext

abstract class ExternalServiceIntegration {

  val OGP_PARAM_NAME = "o"

  def onPostCreated(author: User, article: Article)(implicit ctx: SkinnyContext): Unit
  def onCommentCreated(commenter: User, comment: Comment)(implicit ctx: SkinnyContext): Unit

}
