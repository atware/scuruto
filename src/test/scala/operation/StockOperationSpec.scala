package operation

import controller.IdParamType
import lib.NotificationType
import model._
import model.typebinder.{ ArticleId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.controller.Params

class StockOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new StockOperationImpl

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

  describe("exists") {
    it("should return true if already stocked") { implicit session =>
      val params = Params(Map(
        "user_id" -> uid2.value,
        "article_id" -> aid1.value
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.exists(permittedParameters) shouldBe true
    }
    it("should return false if not liked") { implicit session =>
      val params = Params(Map(
        "user_id" -> uid2.value,
        "article_id" -> aid2.value
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.exists(permittedParameters) shouldBe false
    }
  }

  describe("stock") {
    it("should create stock") { implicit session =>
      val params = Params(Map(
        "user_id" -> uid2,
        "article_id" -> aid2
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.stock(Article.findById(aid2).get, permittedParameters) should equal(2)
      Article.findById(aid2).get.stocksCount should equal(2)

      val notification = Notification.findBy(sqls.eq(Notification.column.userId, authorId.value)).get
      notification.articleId should equal(aid2)
      notification.fragmentId shouldBe None
      notification.senderId should equal(uid2)
      notification.`type` should equal(NotificationType.Stock.value)
    }

    it("should not notify when author stocked") { implicit session =>
      val params = Params(Map(
        "user_id" -> authorId,
        "article_id" -> aid2
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.stock(Article.findById(aid2).get, permittedParameters) should equal(2)
      Article.findById(aid2).get.stocksCount should equal(2)

      Notification.findBy(sqls.eq(Notification.column.userId, authorId)) shouldBe None
    }
  }

  describe("unstock") {
    it("should delete stock") { implicit session =>
      val counter = operation.unstock(User.findById(uid2).get, aid1)
      counter should equal(2)
      Article.findById(aid1).get.stocksCount should equal(2)
    }
  }

  describe("getStockers") {
    it("should return all stockers") { implicit session =>
      val stockers = operation.getStockers(aid1)
      stockers should have size 3
      stockers.head.name should equal("hoge3")
      stockers.last.name should equal("hoge1")
    }
  }

}