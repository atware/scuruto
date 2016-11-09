package model.typebinder

import scalikejdbc._

case class UserId(value: Long) {
  override def toString = value.toString
}

object UserId {
  implicit val longTypeBinder: TypeBinder[UserId] = TypeBinder.long.map(UserId.apply)
  implicit val converter: Binders[UserId] = Binders.long.xmap(UserId.apply, _.value)
}
