package integration

import model.{ Article, Comment, User }
import skinny.logging.LoggerProvider
import skinny.micro.context.SkinnyContext

object LoggerIntegration extends ExternalServiceIntegration with LoggerProvider {

  override def onPostCreated(author: User, article: Article)(implicit ctx: SkinnyContext): Unit = {
    logger.info("onPostCreated fired.")
  }

  override def onCommentCreated(commenter: User, comment: Comment)(implicit ctx: SkinnyContext): Unit = {
    logger.info("onCommentCreated fired.")
  }

}
