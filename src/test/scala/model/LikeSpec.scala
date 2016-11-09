package model

import model.typebinder.{ ArticleId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class LikeSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1: UserId = _
  var aid1, aid2: ArticleId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    val uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "ja", u.isActive -> true)
    val uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true)
    val a = Article.column
    aid1 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0)
    val l = Like.column
    Like.createWithNamedValues(l.userId -> uid2, l.articleId -> aid1)
    Like.createWithNamedValues(l.userId -> uid1, l.articleId -> aid1)
    Like.createWithNamedValues(l.userId -> uid3, l.articleId -> aid1)
  }

  describe("countByArticleId") {
    it("should return count") { implicit session =>
      Like.countByArticleId(aid1) should equal(3)
      Like.countByArticleId(aid2) should equal(0)
    }
  }

  describe("exists") {
    it("should return true if a user liked") { implicit session =>
      Like.exists(uid1, aid1) shouldBe true
    }
    it("should return false if a user did not like") { implicit session =>
      Like.exists(uid1, aid2) shouldBe false
    }
  }

  describe("delete") {
    it("should unlike") { implicit session =>
      Like.delete(uid1, aid1)
      Like.exists(uid1, aid1) shouldBe false
    }
  }

}