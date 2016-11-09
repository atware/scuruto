package model

import model.typebinder._
import org.joda.time._
import scalikejdbc._
import skinny.Pagination
import skinny.orm._
import skinny.orm.feature._

case class Stock(
  stockId: Long,
  userId: UserId,
  articleId: ArticleId,
  createdAt: Option[DateTime] = None,
  updatedAt: Option[DateTime] = None,
  article: Option[Article] = None,
  user: Option[User] = None
)

object Stock extends SkinnyCRUDMapper[Stock] with TimestampsFeature[Stock] {
  override lazy val tableName = "stocks"
  override lazy val defaultAlias = createAlias("s")
  override lazy val primaryKeyFieldName = "stockId"

  val s = defaultAlias

  val articlesRef = hasOneWithFkAndJoinCondition[Article](
    right = Article,
    fk = Stock.column.articleId,
    on = sqls.eq(s.column(column.articleId), Article.a.column(Article.column.articleId)),
    merge = (s, a) => s.copy(article = a)
  )

  val userRef = belongsToWithFk[User](
    right = User,
    fk = column.userId,
    merge = (s, u) => s.copy(user = u)
  )

  def countByArticleId(articleId: ArticleId)(implicit s: DBSession = autoSession): Long = {
    Stock.countBy(sqls.eq(column.articleId, articleId))
  }

  def countByUserId(userId: UserId)(implicit s: DBSession = autoSession): Long = {
    Stock.countBy(sqls.eq(column.userId, userId))
  }

  def exists(userId: UserId, articleId: ArticleId)(implicit s: DBSession = autoSession): Boolean = {
    Stock.countBy(sqls.eq(column.userId, userId).and.eq(column.articleId, articleId)) > 0
  }

  def delete(userId: UserId, articleId: ArticleId)(implicit s: DBSession = autoSession): Unit = {
    Stock.deleteBy(sqls.eq(column.userId, userId).and.eq(column.articleId, articleId))
  }

  def findAllByUserIdWithPaginationOrderByDesc(userId: UserId, pageNo: Int, pageSize: Int)(implicit s: DBSession = autoSession): Seq[Stock] = {
    Stock.paginate(Pagination.page(pageNo).per(pageSize)).where(sqls.eq(column.userId, userId)).orderBy(column.stockId.desc).apply()
  }

  def findByArticleIdOrderByStockIdDesc(articleId: ArticleId)(implicit session: DBSession = autoSession): Seq[Stock] = {
    Stock.joins(Stock.userRef)
      .where(sqls.eq(column.articleId, articleId))
      .orderBy(s.stockId.desc)
      .apply()
  }

  override def extract(rs: WrappedResultSet, s: ResultName[Stock]): Stock = autoConstruct(rs, s, "article", "user")
}
