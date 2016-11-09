package view_model

case class UserWithStats(
  profile: model.User,
  contribution: Long,
  stockedCount: Long,
  likedCount: Long
)

object UserWithStats {}
