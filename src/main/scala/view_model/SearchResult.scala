package view_model

import model.{ Article, Tag }

case class SearchResult(
  q: String,
  tag: Option[Tag],
  articles: Pagination[Article]
)

object SearchResult {

}