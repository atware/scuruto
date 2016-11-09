package integration

import model.{ Article, Comment, User }
import skinny.micro.context.SkinnyContext

object NullIntegration extends ExternalServiceIntegration {

  override def onCommentCreated(commenter: User, comment: Comment)(implicit ctx: SkinnyContext): Unit = {}

  override def onPostCreated(author: User, article: Article)(implicit ctx: SkinnyContext): Unit = {}

}
