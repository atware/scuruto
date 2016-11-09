package model

import model.typebinder.{ ArticleId, UserId }
import org.scalatest.{ fixture, _ }
import scalikejdbc._
import scalikejdbc.scalatest._

class StockSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1, uid2, uid3, authorId: UserId = _
  var aid1, aid2, aid3, aid4: ArticleId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "hoge1", u.email -> "hoge1@moge.jp", u.locale -> "hg", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "hoge2", u.email -> "hoge2@moge.jp", u.locale -> "mg", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "hoge3", u.email -> "hoge3@moge.jp", u.locale -> "HM", u.isActive -> true)
    authorId = User.createWithNamedValues(u.name -> "author", u.email -> "author@example.com", u.locale -> "jp", u.isActive -> true)

    val a = Article.column
    aid1 = Article.createWithNamedValues(a.userId -> authorId, a.title -> "title1", a.body -> "body1", a.commentsCount -> 0, a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> authorId, a.title -> "title2", a.body -> "body2", a.commentsCount -> 0, a.stocksCount -> 0)
    aid3 = Article.createWithNamedValues(a.userId -> authorId, a.title -> "title3", a.body -> "body3", a.commentsCount -> 0, a.stocksCount -> 0)
    aid4 = Article.createWithNamedValues(a.userId -> authorId, a.title -> "title4", a.body -> "body4", a.commentsCount -> 0, a.stocksCount -> 0)

    val s = Stock.column
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid1)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid2)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid3)
  }

  describe("countByArticleId") {
    it("should return count") { implicit session =>
      Stock.countByArticleId(aid1) should equal(3)
      Stock.countByArticleId(aid2) should equal(1)
      Stock.countByArticleId(aid4) should equal(0)
    }
  }

  describe("countByUserId") {
    it("should return count") { implicit session =>
      Stock.countByUserId(uid3) should equal(3)
      Stock.countByUserId(uid2) should equal(1)
      Stock.countByUserId(uid1) should equal(1)
      Stock.countByUserId(authorId) should equal(0)
    }
  }

  describe("exists") {
    it("should return true if a user stocked") { implicit session =>
      Stock.exists(uid1, aid1) shouldBe true
    }
    it("should return false if a user did not stock") { implicit session =>
      Stock.exists(uid1, aid2) shouldBe false
    }
  }

  describe("delete") {
    it("should unstock") { implicit session =>
      Stock.delete(uid1, aid1)
      Stock.exists(uid1, aid1) shouldBe false
    }
  }

  describe("findAllByUserIdWithPaginationOrderByDesc") {
    it("should return first page user stocked") { implicit session =>
      val page = Stock.findAllByUserIdWithPaginationOrderByDesc(uid3, 1, 2)
      page should have size 2
      page.head.articleId should equal(aid3)
      page.last.articleId should equal(aid2)
    }
    it("should return second page user stocked") { implicit session =>
      val page = Stock.findAllByUserIdWithPaginationOrderByDesc(uid3, 2, 2)
      page should have size 1
      page.head.articleId should equal(aid1)
    }
  }

  describe("findByArticleIdOrderByStockIdDesc") {
    it("should return expected stocks") { implicit session =>
      val articleId = Article.findBy(sqls.eq(Article.column.title, "title1")) match {
        case Some(article) => article.articleId
        case _ => throw new IllegalStateException
      }

      val stocks = Stock.findByArticleIdOrderByStockIdDesc(articleId)
      stocks should have size 3
      stocks.map(_.stockId).reverse should be(sorted)

      import org.scalatest.OptionValues._
      stocks.head.user.value should have('email("hoge3@moge.jp"))
      stocks(1).user.value should have('email("hoge2@moge.jp"))
      stocks(2).user.value should have('email("hoge1@moge.jp"))
    }

    it("should be empty") { implicit session =>
      val articleId = Article.findById(aid4) match {
        case Some(article) => article.articleId
        case _ => throw new IllegalStateException
      }
      val stocks = Stock.findByArticleIdOrderByStockIdDesc(articleId)
      stocks should have size 0
    }
  }
}
