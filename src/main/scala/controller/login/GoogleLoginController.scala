package controller.login

import controller.LoginController
import lib.{ LoginProvidor, SessionAttribute }
import model.User
import skinny.controller.feature.GoogleLoginFeature
import skinny.oauth2.client.google.GoogleUser

object GoogleLoginController extends LoginController with GoogleLoginFeature {

  override def providor: LoginProvidor = LoginProvidor("google")

  override protected def processLogin(): Any = super.loginRedirect

  override protected def processCallback(): Any = super.callback

  // these env variables are expected by default
  // SKINNY_OAUTH2_CLIENT_ID_GOOGLE
  // SKINNY_OAUTH2_CLIENT_SECRET_GOOGLE

  override protected def saveAuthorizedUser(gUser: GoogleUser): Unit = {
    val email: String = gUser.emails.head.value
    val emailDomain: String = email.split("@")(1)
    if (permittedEmailDomains.nonEmpty && !permittedEmailDomains.contains(emailDomain)) {
      haltWithBody(403)
    } else {
      val user: User = User.findActivatedByEmail(email).getOrElse {
        val userId = User.create(gUser)
        User.findById(userId).get
      }
      setCurrentLocale(user.locale)
      skinnySession.setAttribute(SessionAttribute.LoginUser.key, user)
    }
  }

  override def handleWhenLoginFailed(): Any = processHandleWhenLoginFailed()

  override def handleWhenLoginSucceeded(): Any = processHandleWhenLoginSucceeded()

}
