package view_model

import model.Article

case class IndexSidebar(
  contribution: Long,
  populars: Seq[Article],
  contributors: Seq[Map[String, Any]]
)

object IndexSidebar {}