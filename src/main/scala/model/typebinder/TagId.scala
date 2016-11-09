package model.typebinder

import scalikejdbc._

case class TagId(value: Long) {
  override def toString = value.toString
}

object TagId {
  implicit val longTypeBinder: TypeBinder[TagId] = TypeBinder.long.map(TagId.apply)
  implicit val converter: Binders[TagId] = Binders.long.xmap(TagId.apply, _.value)
}
