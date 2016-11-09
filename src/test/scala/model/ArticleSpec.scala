package model

import model.typebinder.{ ArticleId, TagId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc.DBSession
import scalikejdbc.scalatest.AutoRollback

class ArticleSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {

  var uid1, uid2, uid3: UserId = _
  var aid2, aid4, aid6, aid8, aid11: ArticleId = _
  var tid2: TagId = _

  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "en", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true)

    val a = Article.column
    val aid1 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 2)
    val aid3 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title3", a.body -> "body3", a.stocksCount -> 0)
    aid4 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title4", a.body -> "body4", a.stocksCount -> 1)
    val aid5 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title5", a.body -> "body5", a.stocksCount -> 0)
    aid6 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title6", a.body -> "body6", a.stocksCount -> 3)
    val aid7 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title7", a.body -> "body7", a.stocksCount -> 0)
    aid8 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title8", a.body -> "body8", a.stocksCount -> 4)
    val aid9 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title9", a.body -> "body9", a.stocksCount -> 0)
    val aid10 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title10", a.body -> "body10", a.stocksCount -> 0)
    aid11 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title11", a.body -> "body11", a.stocksCount -> 0)

    val t = Tag.column
    val tid1 = Tag.createWithNamedValues(t.name -> "tag1")
    tid2 = Tag.createWithNamedValues(t.name -> "tag2")
    val tid3 = Tag.createWithNamedValues(t.name -> "tag3")

    val at = ArticlesTags.column
    ArticlesTags.createWithNamedValues(at.articleId -> aid1, at.tagId -> tid1)
    ArticlesTags.createWithNamedValues(at.articleId -> aid2, at.tagId -> tid2)
    ArticlesTags.createWithNamedValues(at.articleId -> aid3, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid4, at.tagId -> tid1)
    ArticlesTags.createWithNamedValues(at.articleId -> aid5, at.tagId -> tid2)
    ArticlesTags.createWithNamedValues(at.articleId -> aid6, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid7, at.tagId -> tid1)
    ArticlesTags.createWithNamedValues(at.articleId -> aid8, at.tagId -> tid2)
    ArticlesTags.createWithNamedValues(at.articleId -> aid9, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid10, at.tagId -> tid1)
    ArticlesTags.createWithNamedValues(at.articleId -> aid11, at.tagId -> tid2)

    val s = Stock.column
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid4)
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid6)
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid8)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid8)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid2)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid2)
  }

  describe("countByUserId") {
    it("should return articles count") { implicit session =>
      Article.countByUserId(uid1) should equal(1)
      Article.countByUserId(uid2) should equal(0)
      Article.countByUserId(uid3) should equal(10)
    }
  }

  describe("findPageOrderByDesc") {
    it("should return first page") { implicit session =>
      val page = Article.findPageOrderByDesc(pageSize = 10)
      page should have size 10
      page.head.title should equal("title11")
      page.last.title should equal("title2")
    }
    it("should return second page") { implicit session =>
      val page = Article.findPageOrderByDesc(maxId = aid2, pageSize = 10)
      page should have size 1
      page.head.title should equal("title1")
    }
  }

  describe("findPopulars") {
    it("should return top 3") { implicit session =>
      val top5 = Article.findPopulars(5)
      top5 should have size 4
      top5.head.title should equal("title8")
      top5(1).title should equal("title6")
      top5(2).title should equal("title2")
      top5(3).title should equal("title4")
      // should not contains 0 stock article
    }
  }

  describe("findAllByUserIdWithPagination") {
    it("should return first page") { implicit session =>
      val page = Article.findAllByUserIdWithPagination(uid3, 1, 5)
      page should have size 5
      page.head.title should equal("title11")
      page.last.title should equal("title7")
    }
    it("should return second page") { implicit session =>
      val page = Article.findAllByUserIdWithPagination(uid3, 2, 5)
      page should have size 5
      page.head.title should equal("title6")
      page.last.title should equal("title1")
    }
  }

  describe("findAllByIdsOrderByDesc") {
    it("should return articles of ids") { implicit session =>
      val articles = Article.findAllByIdsOrderByDesc(aid2, aid11)
      articles should have size 2
      articles.head.articleId should equal(aid11)
      articles.last.articleId should equal(aid2)
    }
  }

  describe("findAllByIdsOrderByStockDesc") {
    it("should return stocked articles") { implicit session =>
      val articles = Article.findAllByIdsOrderByStockDesc(uid1, aid4, aid6, aid8)
      articles should have size 3
      articles.head.articleId should equal(aid8)
      articles.last.articleId should equal(aid4)
    }
  }

  describe("countByTitleOrBody") {
    it("should return matching count like title or body") { implicit session =>
      val count = Article.countByTitleOrBody("1")
      count should equal(3)
    }
    it("should return 0") { implicit session =>
      val count = Article.countByTitleOrBody("X")
      count should equal(0)
    }
  }

  describe("searchByTitleOrBody") {
    it("should return first page") { implicit session =>
      val articles = Article.searchByTitleOrBody("1", 1, 2)
      articles should have size 2
      articles.head.title should equal("title11")
      articles.last.title should equal("title10")
    }
    it("should return second page") { implicit session =>
      val articles = Article.searchByTitleOrBody("1", 2, 2)
      articles should have size 1
      articles.head.title should equal("title1")
    }
  }

  describe("findByTagId") {
    it("should return articles that has specific tag") { implicit session =>
      val articles = Article.findByTagId(tid2, 3)
      articles should have size 3
      articles.head.get("title").get should equal("title8")
      articles.last.get("title").get should equal("title11")
    }
  }

}
