package view_model

case class Pagination[+A](
  currentPage: Int,
  totalPages: Int,
  totalCount: Long,
  data: Seq[A]
)

object Pagination {}