package model.typebinder

import scalikejdbc._

case class TagId(value: Long) {
  override def toString = value.toString
}

object TagId {
  implicit val converter: Binders[TagId] = Binders.long.xmap(TagId.apply, _.value)
}
