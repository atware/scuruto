package model

import model.typebinder.{ ArticleId, CommentId, UserId }
import org.scalatest._
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class CommentSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1: UserId = _
  var aid1, aid2: ArticleId = _
  var cid2: CommentId = _
  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    val uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "ja", u.isActive -> true)
    val a = Article.column
    aid1 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0)
    val c = Comment.column
    Comment.createWithNamedValues(c.userId -> uid2, c.articleId -> aid1, c.body -> "comment1")
    cid2 = Comment.createWithNamedValues(c.userId -> uid1, c.articleId -> aid1, c.body -> "comment2")
    Comment.createWithNamedValues(c.userId -> uid2, c.articleId -> aid1, c.body -> "comment3")
  }

  describe("countByArticleId") {
    it("should return count") { implicit session =>
      Comment.countByArticleId(aid1) should equal(3)
      Comment.countByArticleId(aid2) should equal(0)
    }
  }

  describe("findAllByArticleId") {
    it("should return comments of an article") { implicit session =>
      val comments = Comment.findAllByArticleId(aid1)
      comments should have size 3
      comments.head.body should equal("comment1")
      comments(1).body should equal("comment2")
      comments(2).body should equal("comment3")
    }
  }

  describe("delete") {
    it("should delete specific comment") { implicit session =>
      val count = Comment.delete(uid1, cid2)
      count should equal(1)
      Comment.findById(cid2) shouldBe None
    }
  }

}