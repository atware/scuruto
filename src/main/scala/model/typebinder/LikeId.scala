package model.typebinder

import scalikejdbc._

case class LikeId(value: Long) {
  override def toString = value.toString
}

object LikeId {
  implicit val converter: Binders[LikeId] = Binders.long.xmap(LikeId.apply, _.value)
}
