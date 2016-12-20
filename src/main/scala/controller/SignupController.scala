package controller

import java.util.Locale

import lib.SessionAttribute
import model.User
import operation.UserOperation
import skinny._
import skinny.mailer.SkinnyMailer
import skinny.validator._

class SignupController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /signup/new
  def input = {
    loginUser match {
      case Some(_) => redirect302("/")
      case _ => render(s"/signup/new")
    }
  }

  // --------------
  // POST /signup/new
  def createParams: Params = Params(params)
  def createForm: MapValidator = validation(
    createParams,
    paramKey("email") is required & email & maxLength(200),
    paramKey("password") is required & maxLength(200)
  )
  def createFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "email" -> ParamType.String,
    "password" -> ParamType.String
  )
  class ConfirmationMailer extends SkinnyMailer
  def create = {
    loginUser match {
      case Some(_) => redirect302("/")
      case _ => {
        debugLoggingParameters(createForm)
        if (createForm.validate()) {
          val email = getRequiredParam[String]("email")
          val domain = email.split("@")(1)
          if (permittedEmailDomains.isEmpty || permittedEmailDomains.contains(domain)) {
            val userOperation = inject[UserOperation]
            val mailer = new ConfirmationMailer
            userOperation.get(email) match {
              case Some(user) =>
                if (user.isActive) {
                  // send already registered mail
                  mailer
                    .to(email)
                    .subject(alreadyRegisteredMailSubject)
                    .body(alreadyRegisteredMailBody)
                    .deliver()
                } else {
                  // update to confirmable
                  val permittedParameters = createParams.permit(createFormStrongParameters: _*)
                  val updated = userOperation.updateToConfirmable(user, permittedParameters, currentLocale.getOrElse(Locale.JAPANESE))
                  // send confirmation mail
                  mailer
                    .to(email)
                    .subject(confirmationMailSubject)
                    .body(confirmationMailBody(updated))
                    .deliver()
                }
              case _ =>
                // register
                val permittedParameters = createParams.permit(createFormStrongParameters: _*)
                val user = userOperation.register(permittedParameters, currentLocale.getOrElse(Locale.JAPANESE))
                // send confirmation mail
                mailer
                  .to(email)
                  .subject(confirmationMailSubject)
                  .body(confirmationMailBody(user))
                  .deliver()
            }
          }
          flash += ("email" -> email)
          redirect302(url(Controllers.signup.createRedirectUrl))
        } else {
          render(s"/signup/new")
        }
      }
    }
  }

  // --------------
  // GET /signup/new/_
  def createRedirect = {
    set("email", flash.get("email").getOrElse("your email address"))
    render(s"/signup/sent")
  }

  private def alreadyRegisteredMailSubject: String = {
    createI18n().getOrKey("signup.confirmation.subject")
  }

  private def alreadyRegisteredMailBody: String = {
    createI18n().getOrKey("signup.confirmation.body.alreadyRegistered")
  }

  private def confirmationMailSubject: String = {
    createI18n().getOrKey("signup.confirmation.subject")
  }

  private def confirmationMailBody(user: User): String = {
    s"""${createI18n().getOrKey("signup.confirmation.body.intro")}
       |
       |$baseURL/signup/verify/${user.confirmationToken.get}
       |
       |${createI18n().getOrKey("signup.confirmation.body.end")}
    """.stripMargin
  }

  // --------------
  // GET /signup/verify/{token}
  def verify(token: String) = {
    val userOperation = inject[UserOperation]
    userOperation.getVerifyable(token) match {
      case Some(user) => {
        val activated = userOperation.activate(user.userId)
        skinnySession.setAttribute(SessionAttribute.LoginUser.key, activated)
        redirect302("/")
      }
      case _ => {
        flash += ("warn" -> createI18n().getOrKey("signup.verify.invalidToken"))
        redirect302(url(Controllers.signup.inputUrl))
      }
    }
  }

}
