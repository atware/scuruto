package controller.login

import controller.LoginController
import lib.{ LoginProvidor, SessionAttribute }
import lib.ldap.{ LDAPAuthenticator, LDAPSetting, LDAPType }
import model.User
import skinny._
import skinny.validator._

object LdapLoginController extends LoginController with LoggerProvider {

  override def providor: LoginProvidor = LoginProvidor("ldap")

  def loginParams: Params = Params(params)
  def loginForm: MapValidator = validation(
    loginParams,
    paramKey("username") is required & maxLength(200),
    paramKey("password") is required & maxLength(200)
  )

  lazy val ldapSetting = LDAPSetting(
    host = SkinnyConfig.stringConfigValue("login.ldap.host").get,
    port = SkinnyConfig.intConfigValue("login.ldap.port").get,
    ldapType = LDAPType(SkinnyConfig.stringConfigValue("login.ldap.type").getOrElse("plain")),
    bindDN = SkinnyConfig.stringConfigValue("login.ldap.bindDN").get,
    bindPassword = SkinnyConfig.stringConfigValue("login.ldap.bindPassword").get,
    baseDN = SkinnyConfig.stringConfigValue("login.ldap.baseDN").get,
    userNameAttribute = SkinnyConfig.stringConfigValue("login.ldap.userNameAttribute").get,
    mailAddressAttribute = SkinnyConfig.stringConfigValue("login.ldap.mailAddressAttribute").get
  )

  override protected def processLogin(): Any = {
    debugLoggingParameters(loginForm)
    if (loginForm.validate()) {
      val userName = loginParams.getAs[String]("username").get
      val password = loginParams.getAs[String]("password").get

      logger.debug(ldapSetting)

      LDAPAuthenticator.authenticateUser(ldapSetting, userName, password) match {
        case Right(ldapUser) => {
          // save user if user doesn't  exists in DB.
          val user = User.findActivatedByEmail(ldapUser.email).getOrElse {
            val userId = User.createFromLdap(ldapUser)
            User.findById(userId).get
          }
          setCurrentLocale(user.locale)
          skinnySession.setAttribute(SessionAttribute.LoginUser.key, user)
          processHandleWhenLoginSucceeded()
        }
        case Left(error) => {
          logger.info(error)
          processHandleWhenLoginFailed()
        }
      }
    } else {
      set("ref", params.get("ref"))
      render(s"/login")
    }
  }

  override protected def processHandleWhenLoginFailed(): Any = {
    flash += ("warn" -> createI18n().getOrKey("login.ldap.failed"))
    set("ref", params.get("ref"))
    render(s"/login")
  }
  override protected def processCallback(): Any = haltWithBody(404)

}
