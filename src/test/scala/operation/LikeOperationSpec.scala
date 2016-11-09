package operation

import controller.IdParamType
import lib.NotificationType
import model._
import model.typebinder.{ ArticleId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.controller.Params

class LikeOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new LikeOperationImpl

  var uid1, uid2, uid3: UserId = _
  var aid1, aid2: ArticleId = _
  override def fixture(implicit session: DBSession) {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true)
    val a = Article.column
    aid1 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0)
    val l = Like.column
    Like.createWithNamedValues(l.userId -> uid2, l.articleId -> aid1)
    Like.createWithNamedValues(l.userId -> uid1, l.articleId -> aid1)
    Like.createWithNamedValues(l.userId -> uid3, l.articleId -> aid1)
  }

  describe("exists") {
    it("should return true if already liked") { implicit session =>
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

  describe("like") {
    it("should create like") { implicit session =>
      val params1 = Params(Map(
        "user_id" -> uid2,
        "article_id" -> aid2
      ))
      val permittedParameters1 = params1.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.like(Article.findById(aid2).get, permittedParameters1) should equal(1)
      Article.findById(aid2).get.likesCount should equal(1)

      val notification = Notification.findBy(sqls.eq(Notification.column.userId, uid1)).get
      notification.articleId should equal(aid2)
      notification.fragmentId shouldBe None
      notification.senderId should equal(uid2)
      notification.`type` should equal(NotificationType.Like.value)

      // --------
      val params2 = Params(Map(
        "user_id" -> uid3,
        "article_id" -> aid2
      ))
      val permittedParameters2 = params2.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.like(Article.findById(aid2).get, permittedParameters2) should equal(2)
      Article.findById(aid2).get.likesCount should equal(2)
    }

    it("should not notify when author liked") { implicit session =>
      val params = Params(Map(
        "user_id" -> uid1,
        "article_id" -> aid2
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId
      ): _*)
      operation.like(Article.findById(aid2).get, permittedParameters) should equal(1)
      Article.findById(aid2).get.likesCount should equal(1)

      Notification.findBy(sqls.eq(Notification.column.userId, uid1)) shouldBe None
    }
  }

  describe("unlike") {
    it("should delete like") { implicit session =>
      val counter = operation.unlike(User.findById(uid2).get, aid1)
      counter should equal(2)
      Article.findById(aid1).get.likesCount should equal(2)
    }
  }

}