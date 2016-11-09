package operation

import model.User._
import model.{ Article, Tag }
import scalikejdbc.DBSession
import view_model._

/**
 * The operation for search articles.
 */
sealed trait SearchOperation {
  val PAGE_SIZE: Int = 20

  def search(q: String, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession = autoSession): SearchResult

}

class SearchOperationImpl extends SearchOperation {

  override def search(q: String, pageNo: Int, pageSize: Int = PAGE_SIZE)(implicit s: DBSession): SearchResult = {
    SearchResult(
      q,
      Tag.findByName(q),
      {
        val totalCount = Article.countByTitleOrBody(q)
        val totalPages = (totalCount / pageSize).toInt + (if (totalCount % pageSize == 0) 0 else 1)
        val articles = Article.searchByTitleOrBody(q, pageNo, pageSize)
        Pagination(pageNo, totalPages, totalCount, articles)
      }
    )
  }

}