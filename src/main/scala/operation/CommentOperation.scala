package operation

import lib.NotificationType
import model.User._
import model._
import model.typebinder.{ ArticleId, CommentId }
import org.joda.time.DateTime
import scalikejdbc.DBSession
import skinny.PermittedStrongParameters

/**
 * The operation for comments.
 */
sealed trait CommentOperation extends OperationBase {

  def getAll(articleId: ArticleId)(implicit s: DBSession = autoSession): Seq[Comment]

  def get(commentId: CommentId)(implicit s: DBSession = autoSession): Option[Comment]
  def create(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = autoSession): Comment
  def update(origin: Comment, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = autoSession): Comment
  def delete(user: User, commentId: CommentId)(implicit s: DBSession = autoSession): Long //returns comments_count

}

class CommentOperationImpl extends CommentOperation {

  override def getAll(articleId: ArticleId)(implicit s: DBSession = autoSession): Seq[Comment] = {
    Comment.findAllByArticleId(articleId)
  }

  override def get(commentId: CommentId)(implicit s: DBSession = autoSession): Option[Comment] = {
    Comment.findById(commentId)
  }

  override def create(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = autoSession): Comment = {
    // commnet
    val commentId = Comment.createWithPermittedAttributes(permittedAttributes)

    // counter
    val counter = Comment.countByArticleId(article.articleId)
    Article.updateById(article.articleId).withAttributes('comments_count -> counter, 'updated_at -> DateTime.now)

    // notification (ignore errors)
    try {
      val commenterId = CommentId(getParameterAsLong("user_id", permittedAttributes))
      // to author
      if (article.userId.value != commenterId.value) {
        Notification.createWithAttributes(
          'user_id -> article.userId.value,
          'article_id -> article.articleId.value,
          'fragment_id -> commentId.value,
          'sender_id -> commenterId.value,
          'type -> NotificationType.Comment.value
        )
      }
      // to commenters
      Comment.findAllByArticleId(article.articleId)
        .map { _.userId }
        .toSet
        .filter(cid => (cid.value != commenterId.value) && (cid != article.userId))
        .map { cid =>
          Notification.createWithAttributes(
            'user_id -> cid.value,
            'article_id -> article.articleId.value,
            'fragment_id -> commentId.value,
            'sender_id -> commenterId.value,
            'type -> NotificationType.Reply.value
          )
        }
      //      }
    } catch {
      case e: Throwable => {
        logger.error(s"Failed to find an article; article_id=$article.articleId", e)
      }
    }

    Comment.findById(commentId).get
  }

  override def update(origin: Comment, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = autoSession): Comment = {
    val commentId = origin.commentId

    Comment.updateById(commentId).withPermittedAttributes(permittedAttributes)

    // history
    CommentHistory.createWithAttributes(
      'comment_id -> commentId.value,
      'new_body -> getParameterAsString("body", permittedAttributes),
      'old_body -> origin.body
    )

    get(commentId).get
  }

  override def delete(user: User, commentId: CommentId)(implicit s: DBSession = autoSession): Long = {
    Comment.findById(commentId).map { comment =>
      val deletedCount = Comment.delete(user.userId, commentId)

      // history
      if (deletedCount > 0) {
        CommentHistory.createWithAttributes(
          'comment_id -> comment.commentId.value,
          'old_body -> comment.body,
          'deleted -> true
        )
      }

      val counter = Comment.countByArticleId(comment.articleId)
      Article.updateById(comment.articleId).withAttributes('comments_count -> counter, 'updated_at -> DateTime.now)
      counter

    } getOrElse 0
  }

}