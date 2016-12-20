package controller

import lib.{ LoginProvidor, SessionAttribute, Util }
import skinny.routing.Routes

abstract class LoginController extends ApplicationController with Routes {
  protectFromForgery()

  def providor: LoginProvidor

  val loginUrl = post("/session") {
    params.get("ref").foreach { ref =>
      skinnySession.setAttribute(SessionAttribute.Ref.key, ref)
    }
    processLogin()
  }.as('login)

  val loginCallbackUrl = get("/session/callback")(processCallback()).as('loginCallback)

  val logoutUrl = delete("/session")(logout()).as('logout)

  protected def processLogin(): Any

  protected def processCallback(): Any

  def redirectURI: String = Util.baseURL(request) + "/session/callback"

  protected def processHandleWhenLoginFailed(): Any = {
    flash += ("warn" -> createI18n().getOrKey("login.failed"))
    redirect302("/")
  }

  protected def processHandleWhenLoginSucceeded(): Any = {
    skinnySession.getAttribute(SessionAttribute.Ref.key).asInstanceOf[Option[String]] match {
      case Some(ref) => {
        skinnySession.removeAttribute(SessionAttribute.Ref.key)
        redirect302(ref)
      }
      case _ => redirect302("/")
    }
  }

  def logout(): Unit = skinnySession.invalidate()

}
