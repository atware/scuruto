package model.typebinder

import scalikejdbc._

case class CommentId(value: Long) {
  override def toString = value.toString
}

object CommentId {
  implicit val longTypeBinder: TypeBinder[CommentId] = TypeBinder.long.map(CommentId.apply)
  implicit val converter: Binders[CommentId] = Binders.long.xmap(CommentId.apply, _.value)
}
