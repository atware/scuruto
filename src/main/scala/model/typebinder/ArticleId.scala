package model.typebinder

import scalikejdbc._

case class ArticleId(value: Long) {
  override def toString = value.toString
}

object ArticleId {
  implicit val converter: Binders[ArticleId] = Binders.long.xmap(ArticleId.apply, _.value)
}
