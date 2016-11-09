package controller.login

import com.github.t3hnar.bcrypt._
import controller.LoginController
import lib.{ LoginProvidor, SessionAttribute }
import model.User
import skinny._
import skinny.validator._

object AppLoginController extends LoginController {

  override def providor: LoginProvidor = LoginProvidor("app")

  def loginParams: Params = Params(params)
  def loginForm: MapValidator = validation(
    loginParams,
    paramKey("email") is required & email & maxLength(200),
    paramKey("password") is required & maxLength(200)
  )
  override protected def processLogin(): Any = {
    debugLoggingParameters(loginForm)
    if (loginForm.validate()) {
      val email = loginParams.getAs[String]("email").get
      val emailDomain = email.split("@")(1)
      if (permittedEmailDomains.nonEmpty && !permittedEmailDomains.contains(emailDomain)) {
        haltWithBody(403)
      } else {
        User.findActivatedByEmail(email).map { user =>
          user.password.map { password =>
            val inputPassword = getRequiredParam[String]("password")
            if (inputPassword.isBcrypted(password)) {
              setCurrentLocale(user.locale)
              skinnySession.setAttribute(SessionAttribute.LoginUser.key, user)
              processHandleWhenLoginSucceeded()
            } else processHandleWhenLoginFailed()
          } getOrElse processHandleWhenLoginFailed()
        } getOrElse processHandleWhenLoginFailed()
      }
    } else {
      set("ref", params.get("ref"))
      render(s"/login")
    }
  }

  override protected def processHandleWhenLoginFailed(): Any = {
    flash += ("warn" -> createI18n().getOrKey("login.failed"))
    set("ref", params.get("ref"))
    render(s"/login")
  }

  override protected def processCallback(): Any = haltWithBody(404)

}
