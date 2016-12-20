package controller

import lib.SessionAttribute
import model.User
import operation.UserOperation
import skinny._
import skinny.mailer.SkinnyMailer
import skinny.validator._

class RecoverController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /recover
  def input = {
    loginUser match {
      case Some(user) => redirect302("/")
      case _ => render(s"/recover/input")
    }
  }

  // --------------
  // POST /recover
  def recoverParams: Params = Params(params)
  def recoverForm: MapValidator = validation(
    recoverParams,
    paramKey("email") is required & email & maxLength(200)
  )
  class RecoverMailer extends SkinnyMailer
  def recover = {
    loginUser match {
      case Some(user) => redirect302("/")
      case _ => {
        debugLoggingParameters(recoverForm)
        if (recoverForm.validate()) {
          val email = getRequiredParam[String]("email")
          val domain = email.split("@")(1)
          if (permittedEmailDomains.isEmpty || permittedEmailDomains.contains(domain)) {
            val userOperation = inject[UserOperation]
            val mailer = new RecoverMailer
            userOperation.get(email).foreach { user =>
              if (user.isActive) {
                // send recover mail
                val resettable = userOperation.updateToPasswordResettable(user)
                mailer
                  .to(email)
                  .subject(recoverMailSubject)
                  .body(recoverMailBody(resettable))
                  .deliver()
              }
            }
          }
          flash += ("email" -> email)
          redirect302(url(Controllers.recover.recoverRedirectUrl))
        } else {
          render(s"/recover/input")
        }
      }
    }
  }

  // --------------
  // GET /recover/_
  def recoverRedirect = {
    set("email", flash.get("email").getOrElse("your email address"))
    render(s"/recover/sent")
  }

  private def recoverMailSubject: String = {
    createI18n().getOrKey("recover.confirmation.subject")
  }

  private def recoverMailBody(user: User): String = {
    s"""${createI18n().getOrKey("recover.confirmation.body.intro")}
       |
       |$baseURL/recover/verify/${user.resetPasswordToken.get}
       |
       |${createI18n().getOrKey("recover.confirmation.body.end")}
    """.stripMargin
  }

  // --------------
  // GET /signup/verify/{token}
  def verify(token: String) = {
    val userOperation = inject[UserOperation]
    userOperation.getPasswordResettable(token).map { user =>
      loginUser match {
        case Some(_) => redirect302("/")
        case _ => {
          skinnySession.setAttribute(SessionAttribute.ResetPasswordToken.key, token)
          render(s"/recover/password")
        }
      }
    } getOrElse {
      flash += ("warn" -> createI18n().getOrKey("recover.verify.invalidToken"))
      redirect302(url(Controllers.recover.inputUrl))
    }
  }

  // --------------
  // POST /recover/password
  def resetParams: Params = Params(params)
  def resetForm: MapValidator = validation(
    resetParams,
    paramKey("password") is required & maxLength(200),
    param("confirm password" -> (params("password"), params("confirm"))) are same
  )
  def reset = {
    if (resetForm.validate()) {
      skinnySession.getAttribute(SessionAttribute.ResetPasswordToken.key) match {
        case Some(token) => {
          val userOperation = inject[UserOperation]
          userOperation.getPasswordResettable(token.asInstanceOf[String]) match {
            case Some(user) => {
              userOperation.resetPassword(user, getRequiredParam[String]("password"))
              flash += ("warn" -> createI18n().getOrKey("recover.password.complete"))
              redirect302("/")
            }
            case _ => {
              flash += ("warn" -> createI18n().getOrKey("recover.verify.invalidToken"))
              redirect302(url(Controllers.recover.inputUrl))
            }
          }
        }
        case _ => redirect302(url(Controllers.recover.inputUrl))
      }
    } else {
      render(s"/recover/password")
    }
  }

}
