package operation

import java.util.Locale

import com.github.t3hnar.bcrypt._
import model._
import model.typebinder.UserId
import org.joda.time.DateTime
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback
import skinny.ParamType
import skinny.controller.Params

class UserOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  val operation = new UserOperationImpl

  var uid1, uid2, uid3: UserId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "en", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true,
      u.resetPasswordToken -> "token3", u.resetPasswordSentAt -> DateTime.parse("2016-01-02T15:04:05"))
    User.createWithNamedValues(u.name -> "苗字 名前4", u.email -> "hoge4@moge.jp", u.locale -> "ja", u.isActive -> false,
      u.confirmationToken -> "token4", u.confirmationSentAt -> DateTime.parse("2016-01-01T15:04:05"))

    val a = Article.column
    val aid1 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title1", a.body -> "body1")
    val aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2")

    val s = Stock.column
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid2)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid2)

    val l = Like.column
    Like.createWithNamedValues(l.userId -> uid1, l.articleId -> aid1)
    Like.createWithNamedValues(l.userId -> uid3, l.articleId -> aid1)
  }

  describe("get") {
    it("should return a user") { implicit session =>
      operation.get("hoge2@moge.jp") match {
        case Some(user) => user.email should equal("hoge2@moge.jp")
        case None => fail()
      }
    }

    it("should not return user") { implicit session =>
      operation.get("xxx@example.com") shouldBe None
    }
  }

  describe("register") {
    it("should create a user") { implicit session =>
      val params = Params(Map(
        "email" -> "foo@example.com",
        "password" -> "password1"
      ))
      val permittedParameters = params.permit(Seq(
        "email" -> ParamType.String,
        "password" -> ParamType.String
      ): _*)
      val user = operation.register(permittedParameters, Locale.JAPANESE)
      user.name should equal("foo")
      user.email should equal("foo@example.com")
      "password1".isBcrypted(user.password.get) shouldBe true
      user.imageUrl should equal(Some("/assets/img/avatar_default.jpg"))
      user.locale should equal("ja")
      user.confirmationToken should not be None
      user.confirmationSentAt should not be None
      user.isActive shouldBe false
    }
  }

  describe("updateToConfirmable") {
    it("should update confirmation token") { implicit session =>
      val params1 = Params(Map(
        "email" -> "foo@example.com",
        "password" -> "password1"
      ))
      val permittedParameters1 = params1.permit(Seq(
        "email" -> ParamType.String,
        "password" -> ParamType.String
      ): _*)
      val user1 = operation.register(permittedParameters1, Locale.JAPANESE)

      val params2 = Params(Map(
        "email" -> "foo@example.com",
        "password" -> "password2"
      ))
      val permittedParameters2 = params2.permit(Seq(
        "email" -> ParamType.String,
        "password" -> ParamType.String
      ): _*)
      val user2 = operation.updateToConfirmable(user1, permittedParameters2, Locale.ENGLISH)

      user2.name should equal("foo")
      user2.email should equal("foo@example.com")
      "password2".isBcrypted(user2.password.get) shouldBe true
      user2.imageUrl should equal(Some("/assets/img/avatar_default.jpg"))
      user2.locale should equal("en")
      user2.confirmationToken should not be None
      user2.confirmationSentAt should not be None
      user2.isActive shouldBe false

      user1.confirmationToken.get should not equal user2.confirmationToken.get
      user2.confirmationSentAt.get.isAfter(user1.confirmationSentAt.get)
    }
  }

  describe("getVerifyable") {
    it("should return a verifyable user") { implicit session =>
      val params1 = Params(Map(
        "email" -> "foo@example.com",
        "password" -> "password1"
      ))
      val permittedParameters1 = params1.permit(Seq(
        "email" -> ParamType.String,
        "password" -> ParamType.String
      ): _*)
      val user1 = operation.register(permittedParameters1, Locale.JAPANESE)

      val verifyable = operation.getVerifyable(user1.confirmationToken.get)
      verifyable should not be None
    }

    it("should not return when token expired") { implicit session =>
      operation.getVerifyable("token4") shouldBe None
    }

    it("should not return when invalid token") { implicit session =>
      operation.getVerifyable("xxxx") shouldBe None
    }
  }

  describe("activate") {
    it("should activate a user") { implicit session =>
      val params = Params(Map(
        "email" -> "foo@example.com",
        "password" -> "password1"
      ))
      val permittedParameters = params.permit(Seq(
        "email" -> ParamType.String,
        "password" -> ParamType.String
      ): _*)
      val user = operation.register(permittedParameters, Locale.JAPANESE)

      val activated = operation.activate(user.userId)
      activated.confirmationToken shouldBe None
      activated.confirmationSentAt shouldBe None
      activated.isActive shouldBe true
    }
  }

  describe("updateToPasswordResettable") {
    it("should update to password resettable") { implicit session =>
      val user = operation.updateToPasswordResettable(User.findById(uid1).get)
      user.resetPasswordToken should not be None
      user.resetPasswordSentAt should not be None
    }
  }

  describe("getPasswordResettable") {
    it("should return a password resettable user") { implicit session =>
      val user = operation.updateToPasswordResettable(User.findById(uid1).get)
      val resettable = operation.getPasswordResettable(user.resetPasswordToken.get)
      resettable should not be None
    }

    it("should not return when token expired") { implicit session =>
      operation.getPasswordResettable("token3") shouldBe None
    }

    it("should not return when invalid token") { implicit session =>
      operation.getPasswordResettable("xxxx") shouldBe None
    }
  }

  describe("resetPassword") {
    it("should update password") { implicit session =>
      val user = User.findById(uid1).get
      val reset = operation.resetPassword(user, "passwordreset")
      "passwordreset".isBcrypted(reset.password.get) shouldBe true
    }
  }

  describe("getWithStats") {
    it("should return a user with stats") { implicit session =>
      operation.getWithStats(uid3) match {
        case Some(uws) =>
          uws.profile.email should equal("hoge3@moge.jp")
          uws.contribution should equal(7L)
        //uws.stockedCount should equal(3)
        //uws.likedCount should equal(2)
        case None => fail()
      }
    }
    it("should be none") { implicit session =>
      operation.getWithStats(UserId(uid3.value + 100)) shouldBe None
    }
  }

  describe("update") {
    it("should update by expected values") { implicit session =>
      val params = Params(Map("comment" -> "comment1", "locale" -> "en"))
      val permittedParameters = params.permit(Seq("comment" -> ParamType.String, "locale" -> ParamType.String): _*)
      val user = operation.update(uid1, permittedParameters)
      user.userId should equal(uid1)
      user.comment should equal(Some("comment1"))
      user.locale should equal("en")
    }

    it("should throw exception when a user not exist") { implicit session =>
      val params = Params(Map("comment" -> "comment1", "locale" -> "en"))
      val permittedParameters = params.permit(Seq("comment" -> ParamType.String, "locale" -> ParamType.String): _*)
      an[NoSuchElementException] should be thrownBy operation.update(UserId(uid1.value + 100), permittedParameters)
    }
  }

}