package operation

import lib.NotificationType
import model._
import model.typebinder.{ ArticleId, UserId }
import org.joda.time.DateTime
import scalikejdbc.DBSession
import skinny.PermittedStrongParameters

/**
 * The operation for stocks.
 */
sealed trait StockOperation extends OperationBase {

  def exists(permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Stock.autoSession): Boolean
  def stock(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Stock.autoSession): Long //returns stocks_count
  def unstock(user: User, articleId: ArticleId)(implicit s: DBSession = Stock.autoSession): Long //returns stocks_count

  def getStockers(articleId: ArticleId)(implicit s: DBSession = Stock.autoSession): Seq[User]

}

class StockOperationImpl extends StockOperation {

  override def exists(permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Stock.autoSession): Boolean = {
    val userId = UserId(getParameterAsLong("user_id", permittedAttributes))
    val articleId = ArticleId(getParameterAsLong("article_id", permittedAttributes))
    Stock.exists(userId, articleId)
  }

  override def stock(article: Article, permittedAttributes: PermittedStrongParameters)(implicit s: DBSession = Stock.autoSession): Long = {
    val userId = UserId(getParameterAsLong("user_id", permittedAttributes))

    // stock
    Stock.createWithPermittedAttributes(permittedAttributes)
    // counter
    val counter = Stock.countByArticleId(article.articleId)
    Article.updateById(article.articleId).withAttributes('stocks_count -> counter, 'updated_at -> DateTime.now)

    // notification (ignore errors)
    try {
      if (article.userId != userId) {
        Notification.createWithAttributes(
          'user_id -> article.userId.value,
          'article_id -> article.articleId.value,
          'sender_id -> userId.value,
          'type -> NotificationType.Stock.value
        )
      }
    } catch {
      case e: Throwable => {
        logger.error(s"Failed to find an article; article_id=$article.articleId", e)
      }
    }
    counter
  }

  override def unstock(user: User, articleId: ArticleId)(implicit s: DBSession = Stock.autoSession): Long = {
    Stock.delete(user.userId, articleId)
    // counter
    val counter = Stock.countByArticleId(articleId)
    Article.updateById(articleId).withAttributes('stocks_count -> counter, 'updated_at -> DateTime.now)
    counter
  }

  override def getStockers(articleId: ArticleId)(implicit s: DBSession = Stock.autoSession): Seq[User] = {
    Stock.findByArticleIdOrderByStockIdDesc(articleId).flatMap(_.user)
  }

}