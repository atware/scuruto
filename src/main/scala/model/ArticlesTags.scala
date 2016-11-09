package model

import model.typebinder._
import scalikejdbc._
import skinny.Pagination
import skinny.orm._

case class ArticlesTags(
  articleId: ArticleId,
  tagId: TagId
)

object ArticlesTags extends SkinnyJoinTable[ArticlesTags] {
  override val tableName = "articles_tags"
  override val defaultAlias = createAlias("at")

  def countByTagId(tagId: TagId)(implicit s: DBSession = autoSession): Long = {
    ArticlesTags.countBy(sqls.eq(ArticlesTags.column.tagId, tagId))
  }

  def deleteAllByArticleId(articleId: ArticleId)(implicit s: DBSession = autoSession): Long = {
    ArticlesTags.deleteBy(sqls.eq(ArticlesTags.column.articleId, articleId))
  }

  def findAllByTagWithPaginationOrderByDesc(tagId: TagId, pageNo: Int, pageSize: Int)(implicit s: DBSession = autoSession): Seq[ArticlesTags] = {
    ArticlesTags.paginate(Pagination.page(pageNo).per(pageSize)).where(sqls.eq(column.tagId, tagId)).orderBy(column.articleId.desc).apply()
  }

  override def extract(rs: WrappedResultSet, at: ResultName[ArticlesTags]): ArticlesTags = autoConstruct(rs, at)
}
