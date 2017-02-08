package lib.ldap

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.scalatest.{ FunSpec, Matchers }
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder

/**
 */
class LDAPAuthenticatorSpec extends FunSpec with Matchers {

  private def embeddedLdapRule() = EmbeddedLdapRuleBuilder.newInstance()
    .bindingToAddress("127.0.0.1")
    .bindingToPort(389)
    .importingLdifs("lib/ldap/ldap_test.ldif").build()

  private def mkSetting(bindPassword: String): LDAPSetting = {
    LDAPSetting("127.0.0.1", 389, Plain, "dc=example,dc=com", "cn=admin,ou=Users,dc=example,dc=com", bindPassword, "uid", "mail")
  }
  // trick for using junit4 rule
  def withRule[T <: TestRule](rule: T)(testCode: T => Any): Unit = {
    rule(
      new Statement() {
        override def evaluate(): Unit = testCode(rule)
      },
      Description.createSuiteDescription("JUnit rule wrapper")
    ).evaluate()
  }

  it("success bindDN and userDN") {
    withRule(embeddedLdapRule()) { _ =>
      val res = LDAPAuthenticator.authenticateUser(mkSetting("adminpass"), "user1", "user1pass")
      res.isRight should equal(true)
      res.right.get should equal(LDAPUser("user1", "user1@example.com", "cn=user1,ou=Users,dc=example,dc=com"))
    }
  }

  it("success bindDN, but user auth is fail by invalid password") {
    withRule(embeddedLdapRule()) { _ =>
      val res = LDAPAuthenticator.authenticateUser(mkSetting("adminpass"), "user1", "invalid")
      res.isLeft should equal(true)
      res.left.get should include("provided password was incorrect")
    }
  }

  it("success bindDN but user not found. ") {
    withRule(embeddedLdapRule()) { _ =>
      val res = LDAPAuthenticator.authenticateUser(mkSetting("adminpass"), "user2", "user2pass")
      res.isLeft should equal(true)
      res.left.get should equal("user not found.")
    }
  }

  it("fail bindDN by invalid password") {
    withRule(embeddedLdapRule()) { _ =>
      val res = LDAPAuthenticator.authenticateUser(mkSetting("invalid"), "user1", "user1pass")
      res.isLeft should equal(true)
      res.left.get should include("password was incorrect")
    }
  }

  it("fail bindDN by invalid dn") {
    withRule(embeddedLdapRule()) { _ =>
      val setting = mkSetting("invalid").copy(bindDN = "cn=notadmin,ou=Users,dc=example,dc=com")
      val res = LDAPAuthenticator.authenticateUser(setting, "user1", "user1pass")
      res.isLeft should equal(true)
      res.left.get should include("no such entry exists")
    }
  }

  it("server connection fail by invalid setting") {
    withRule(embeddedLdapRule()) { _ =>
      val setting = mkSetting("adminpass").copy(port = 900)
      val res = LDAPAuthenticator.authenticateUser(setting, "user1", "user1pass")
      res.isLeft should equal(true)
      res.left.get should include("Connection refused")
    }
  }
}
