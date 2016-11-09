package model.typebinder

import scalikejdbc._

case class LikeId(value: Long) {
  override def toString = value.toString
}

object LikeId {
  implicit val longTypeBinder: TypeBinder[LikeId] = TypeBinder.long.map(LikeId.apply)
  implicit val converter: Binders[LikeId] = Binders.long.xmap(LikeId.apply, _.value)
}
