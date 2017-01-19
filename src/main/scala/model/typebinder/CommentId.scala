package model.typebinder

import scalikejdbc._

case class CommentId(value: Long) {
  override def toString = value.toString
}

object CommentId {
  implicit val converter: Binders[CommentId] = Binders.long.xmap(CommentId.apply, _.value)
}
