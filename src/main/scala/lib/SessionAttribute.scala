package lib

sealed abstract class SessionAttribute(val key: String)

object SessionAttribute {
  case object LoginUser extends SessionAttribute("login_user")
  case object Ref extends SessionAttribute("ref")
  case object UploadPolicy extends SessionAttribute("upload_policy")
  case object ResetPasswordToken extends SessionAttribute("reset_password_token")
}