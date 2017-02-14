package lib.ldap

import javax.swing.plaf.synth.SynthSliderUI

import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest
import com.unboundid.ldap.sdk.{ LDAPConnection, LDAPConnectionOptions, LDAPInterface, SearchScope }
import com.unboundid.util.ssl.{ SSLUtil, TrustStoreTrustManager }
import skinny.logging.LoggerProvider

/**  LDAP Connection type. current Plan only, SST, TLS will be supported in future.*/
sealed trait LDAPType
/** Plain LDAP */
case object Plain extends LDAPType
/** LDAPS */
case object SSL extends LDAPType
/** STARTTLS, In generally, this need keyStore file. */
case object TLS extends LDAPType

object LDAPType {
  def apply(value: String): LDAPType = value.toLowerCase match {
    case "plain" => Plain
    case "ssl" => SSL
    case "tls" => TLS
  }
}

case class LDAPSetting(host: String, port: Int, ldapType: LDAPType, baseDN: String, bindDN: String, bindPassword: String,
  userNameAttribute: String, mailAddressAttribute: String, keyStore: Option[String] = None)

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

    val con = createConnection(setting)
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

  private def createConnection(setting: LDAPSetting): LDAPConnection = (setting.ldapType, setting.keyStore) match {
    case (Plain, _) => new LDAPConnection()
    case (SSL, keyStore) => {
      val ssl = createSSLUtil(keyStore)
      val con = new LDAPConnection()
      con.setSocketFactory(ssl.createSSLSocketFactory())
      con
    }
    case (TLS, keyStore) => {
      val ssl = createSSLUtil(keyStore)
      val con = new LDAPConnection()
      val socket = ssl.createSSLSocketFactory()
      val startTls = new StartTLSExtendedRequest(socket)
      con.processExtendedOperation(startTls)
      con
    }
  }

  private def createSSLUtil(keyStore: Option[String]): SSLUtil = if (keyStore.isDefined) {
    new SSLUtil(new TrustStoreTrustManager(keyStore.get))
  } else {
    new SSLUtil
  }
}