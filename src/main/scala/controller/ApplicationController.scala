package controller

import java.net.URLEncoder

import skinny._
import skinny.controller.feature.ScaldiFeature
import skinny.filter._

/**
 * The base controller for this application.
 */
abstract class ApplicationController extends SkinnyController
    with ControllerBase
    with ScaldiFeature
    with ErrorPageFilter {

  protected val permittedEmailDomains: Seq[String] = {
    SkinnyConfig.stringConfigValue("login.permittedEmailDomains").map { domains =>
      domains.split(",").map(_.trim()).toSeq
    } getOrElse Seq()
  }

  val rootUrlPattern = "(^/$)".r
  val apiUrlPattern = "(^/api/.*)".r
  val signupUrlPattern = "(^/signup.*)".r
  val recoverUrlPattern = "(^/recover.*)".r
  val authUrlPattern = "(^/session.*)".r
  val notificationUrlPattern = "(^/notifications.*)".r
  val articlesUrlPattern = "(^/articles/.*)".r
  beforeAction() {
    val uri: String = request.getRequestURI
    if (loginUser.isDefined) {
      uri match {
        case signupUrlPattern(_) => redirect302("/")
        case recoverUrlPattern(_) => redirect302("/")
        case authUrlPattern(_) => {
          if (request.getMethod.toUpperCase != "DELETE") redirect302("/")
        }
        case _ => /* nop */
      }
    } else {
      uri match {
        case rootUrlPattern(_) => /* nop */
        case apiUrlPattern(_) => redirect302("/")
        case signupUrlPattern(_) => /* nop */
        case recoverUrlPattern(_) => /* nop */
        case authUrlPattern(_) => /* nop */
        case notificationUrlPattern(_) => redirect302("/")
        case articlesUrlPattern(_) =>
          if (request.getParameter("o") == null) {
            redirect302("/?ref=" + getRef)
          } /* else nop */
        case _ => redirect302("/?ref=" + getRef)
      }
    }
    set("loginUser" -> loginUser)
  }

  protected def getRef: String = {
    val uri = request.getRequestURI
    val query = request.getQueryString
    val ref = {
      if (query != null) {
        uri + "?" + query
      } else {
        uri
      }
    }
    if (ref == null) return ""
    URLEncoder.encode(ref, "UTF-8")
  }
}

