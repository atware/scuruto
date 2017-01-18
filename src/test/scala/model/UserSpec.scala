package model

import model.typebinder.UserId
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.oauth2.client.google.GoogleUser

class UserSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1, uid2, uid3, uid4: UserId = _

  override def fixture(implicit session: DBSession): Unit = {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "en", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true, u.resetPasswordToken -> "token3")
    uid4 = User.createWithNamedValues(u.name -> "苗字 名前4", u.email -> "hoge4@moge.jp", u.locale -> "ja", u.isActive -> false, u.confirmationToken -> "token4")

    val a = Article.column
    val aid1 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title", a.body -> "body")
    val aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title", a.body -> "body")

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

  describe("findByEmail") {
    it("should return a active user") { implicit session =>
      val user = User.findByEmail("hoge1@moge.jp")
      user shouldBe defined
      user.get.email should equal("hoge1@moge.jp")
    }

    it("should not return inactive user") { implicit session =>
      val user = User.findByEmail("hoge4@moge.jp")
      user shouldBe defined
      user.get.email should equal("hoge4@moge.jp")
    }

    it("should not return non-registered user") { implicit session =>
      val user = User.findByEmail("hoge5@moge.jp")
      user shouldBe None
    }
  }

  describe("findActivatedByEmail") {
    it("should return an active user") { implicit session =>
      val user = User.findActivatedByEmail("hoge1@moge.jp")
      user shouldBe defined
      user.get.email should equal("hoge1@moge.jp")
    }

    it("should not return inactive user") { implicit session =>
      val user = User.findActivatedByEmail("hoge4@moge.jp")
      user shouldBe None
    }

    it("should not return non-registered user") { implicit session =>
      val user = User.findActivatedByEmail("hoge4@moge.jp")
      user shouldBe None
    }
  }

  describe("calcContribution") {
    it("should be 0") { implicit session =>
      User.calcContribution(uid2) should equal(0)
    }
    it("should be 7 (stocks:6 + likes:1)") { implicit session =>
      User.calcContribution(uid3) should equal(7)
    }
  }

  describe("calcContributionRanking") {
    it("should return top 1 only") { implicit session =>
      val ranking = User.calcContributionRanking(1)
      ranking should have size 1
      ranking.head.get("uid").get should equal(uid3.value)
    }
    it("should return ranking") { implicit session =>
      val ranking = User.calcContributionRanking(5)
      ranking should have size 2
      ranking.head.get("uid").get should equal(uid3.value)
      ranking.last.get("uid").get should equal(uid1.value)
      // should not contains 0 contribution user
    }
  }

  /*
  describe("calcReactions") {
    it("should return reaction count") { implicit session =>
      val reaction1 = User.calcReactions(uid3)
      reaction1._1 should equal(2)
      reaction1._2 should equal(1)
      val reaction2 = User.calcReactions(uid1)
      reaction2._1 should equal(2)
      reaction2._2 should equal(0)
      val reaction3 = User.calcReactions(uid2)
      reaction3._1 should equal(0)
      reaction3._2 should equal(0)
    }
  }
  */

  /* for App login feature */

  describe("findByConfirmationToken") {
    it("should return a confirmable user") { implicit session =>
      val user = User.findByConfirmationToken("token4")
      user shouldBe defined
      user.get.email should equal("hoge4@moge.jp")
    }

    it("should not return a user") { implicit session =>
      val user = User.findByConfirmationToken("xxxxx")
      user shouldBe None
    }
  }

  describe("findByResetPasswordToken") {
    it("should return a password resetable user") { implicit session =>
      val user = User.findByResetPasswordToken("token3")
      user shouldBe defined
      user.get.email should equal("hoge3@moge.jp")
    }

    it("should not return non-registered user") { implicit session =>
      val user = User.findByResetPasswordToken("xxxxx")
      user shouldBe None
    }
  }

  /* for Google login feature */

  describe("create") {
    it("should create expected user") { implicit session =>
      val name: skinny.oauth2.client.google.Name = skinny.oauth2.client.google.Name("namae", "myoji")
      val image: skinny.oauth2.client.google.Image = skinny.oauth2.client.google.Image("http://img.example.com/1?sz=50", isDefault = true)
      val emails: Seq[skinny.oauth2.client.google.Email] = Seq(skinny.oauth2.client.google.Email("foo@example.com", ""))
      val gUser = new GoogleUser("", "", name, Option.empty, Option(image), emails)

      val id = User.create(gUser)

      val user = User.findById(id).get
      user.name should equal("myoji namae")
      user.email should equal("foo@example.com")
      user.imageUrl.get should equal("http://img.example.com/1")
      user.locale should equal("ja")
    }

    it("should create a user by email address") { implicit session =>
      val name: skinny.oauth2.client.google.Name = skinny.oauth2.client.google.Name("", "")
      val image: skinny.oauth2.client.google.Image = skinny.oauth2.client.google.Image("http://img.example.com/1?sz=50", isDefault = true)
      val emails: Seq[skinny.oauth2.client.google.Email] = Seq(skinny.oauth2.client.google.Email("foo@example.com", ""))
      val gUser = new GoogleUser("", "", name, Option.empty, Option(image), emails)

      val id = User.create(gUser)

      val user = User.findById(id).get
      user.name should equal("foo")
      user.email should equal("foo@example.com")
      user.imageUrl.get should equal("http://img.example.com/1")
      user.locale should equal("ja")
    }

  }

}
