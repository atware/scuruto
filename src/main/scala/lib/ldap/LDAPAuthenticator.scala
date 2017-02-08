package lib.ldap

import com.unboundid.ldap.sdk.{ LDAPConnection, LDAPConnectionOptions, LDAPInterface, SearchScope }
import skinny.logging.LoggerProvider

/**  LDAP Connection type. current Plan only, SST, TLS will be supported in future.*/
sealed trait LDAPType
case object Plain extends LDAPType

object LDAPType {
  def apply(value: String): LDAPType = value.toLowerCase match {
    case "plain" => Plain
  }
}

case class LDAPSetting(host: String, port: Int, ldapType: LDAPType, baseDN: String, bindDN: String, bindPassword: String,
  userNameAttribute: String, mailAddressAttribute: String)

case class LDAPUser(name: String, email: String, dn: String, imageUrl: String = "/assets/img/avatar_default.jpg")

/**
 *
 * Authentication tor LDAP
 *
 */
object LDAPAuthenticator extends LoggerProvider {

  def authenticateUser(ldapSetting: LDAPSetting, userName: String, password: String): Either[String, LDAPUser] = {
    // first connect by setting bindDN and get dn of login user.
    val findUser = connect(ldapSetting, ldapSetting.bindDN, ldapSetting.bindPassword) {
      con =>
        {
          val results = con.search(ldapSetting.baseDN, SearchScope.SUB, s"(${ldapSetting.userNameAttribute}=${userName})")
          if (results.getEntryCount == 0) {
            None
          } else {
            val u = results.getSearchEntries.get(0)
            Some(LDAPUser(u.getAttribute(ldapSetting.userNameAttribute).getValue, u.getAttribute(ldapSetting.mailAddressAttribute).getValue, u.getDN))
          }
        }
    }
    // second, check user password by binding by userDN.
    findUser match {
      case e @ Left(_) => e
      case Right(user) => connect(ldapSetting, user.dn, password) { _ => Some(user) }
    }
  }

  private def connect[A](setting: LDAPSetting, bindDN: String, bindPassword: String)(afterConnect: LDAPInterface => Option[A]): Either[String, A] = {

    val con = new LDAPConnection
    try {

      con.connect(setting.host, setting.port)
      con.bind(bindDN, bindPassword)
      afterConnect(con).map(u => Right(u)).getOrElse(Left("user not found."))

    } catch {
      case e: Exception => {

        logger.info("ldap bind fail.", e)
        Left(e.getMessage)
      }
    } finally {
      con.close()
    }
  }

}