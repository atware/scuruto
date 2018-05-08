package operation

import lib.NotificationType
import model.typebinder.{ ArticleId, UserId }
import model.{ Article, Like, Notification, User }
import org.joda.time.DateTime
import scalikejdbc.DBSession
import skinny.PermittedStrongParameters

/**
 * The operation for likes.
 */
sealed trait LikeOperation extends OperationBase {

  def exists(permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Like.autoSession): Boolean
  def like(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Like.autoSession): Long //returns likes_count
  def unlike(user: User, articleId: ArticleId)(implicit s: DBSession = Like.autoSession): Long //returns likes_count

}

class LikeOperationImpl extends LikeOperation {

  override def exists(permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Like.autoSession): Boolean = {
    val userId = UserId(getParameterAsLong("user_id", permittedAttributes))
    val articleId = ArticleId(getParameterAsLong("article_id", permittedAttributes))
    Like.exists(userId, articleId)
  }

  override def like(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Like.autoSession): Long = {
    val userId = UserId(getParameterAsLong("user_id", permittedAttributes))

    // like
    Like.createWithPermittedAttributes(permittedAttributes)
    // counter
    val counter = Like.countByArticleId(article.articleId)
    Article.updateById(article.articleId).withAttributes('likes_count -> counter, 'updated_at -> DateTime.now)

    // notification (ignore errors)
    try {
      if (article.userId != userId) {
        Notification.createWithAttributes(
          'user_id -> article.userId.value,
          'article_id -> article.articleId.value,
          'sender_id -> userId.value,
          'type -> NotificationType.Like.value
        )
      }
    } catch {
      case e: Throwable => {
        logger.error(s"Failed to find an article; article_id=$article.articleId", e)
      }
    }
    counter
  }

  override def unlike(user: User, articleId: ArticleId)(implicit s: DBSession = Like.autoSession): Long = {
    Like.delete(user.userId, articleId)
    // counter
    val counter = Like.countByArticleId(articleId)
    Article.updateById(articleId).withAttributes('likes_count -> counter, 'updated_at -> DateTime.now)
    counter
  }

}