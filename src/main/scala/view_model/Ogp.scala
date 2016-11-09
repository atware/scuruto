package view_model

import model.typebinder.ArticleId

case class Ogp(
  id: ArticleId,
  title: String,
  description: String,
  image: String,
  url: String
)

object Ogp {}