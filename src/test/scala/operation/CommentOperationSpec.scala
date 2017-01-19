package operation

import controller.IdParamType
import lib.NotificationType
import model._
import model.typebinder.{ ArticleId, CommentId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.ParamType
import skinny.controller.Params

class CommentOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new CommentOperationImpl

  var uid1, uid2, uid3: UserId = _
  var aid1, aid2: ArticleId = _
  var cid3: CommentId = _
  override def fixture(implicit session: DBSession): Unit = {
    val u = User.column
    uid1 = User.createWithNamedValues(u.name -> "苗字 名前1", u.email -> "hoge1@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid2 = User.createWithNamedValues(u.name -> "苗字 名前2", u.email -> "hoge2@moge.jp", u.locale -> "ja", u.isActive -> true)
    uid3 = User.createWithNamedValues(u.name -> "苗字 名前3", u.email -> "hoge3@moge.jp", u.locale -> "ja", u.isActive -> true)
    val a = Article.column
    aid1 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title1", a.body -> "body1", a.stocksCount -> 0, a.commentsCount -> 3)
    aid2 = Article.createWithNamedValues(a.userId -> uid1, a.title -> "title2", a.body -> "body2", a.stocksCount -> 0, a.commentsCount -> 0)
    val c = Comment.column
    Comment.createWithNamedValues(c.userId -> uid2, c.articleId -> aid1, c.body -> "comment1")
    Comment.createWithNamedValues(c.userId -> uid1, c.articleId -> aid1, c.body -> "comment2")
    cid3 = Comment.createWithNamedValues(c.userId -> uid2, c.articleId -> aid1, c.body -> "comment3")
  }

  describe("getAll") {
    it("should return all comments of an article") { implicit session =>
      val comments = operation.getAll(aid1)
      comments should have size 3
      comments.head.body should equal("comment1")
      comments(1).body should equal("comment2")
      comments(2).body should equal("comment3")
    }
  }

  describe("get") {
    it("should return specific comment") { implicit session =>
      val comment = operation.get(cid3)
      comment shouldBe defined
      comment.get.userId should equal(uid2)
      comment.get.articleId should equal(aid1)
      comment.get.body should equal("comment3")
    }
    it("should be none") { implicit session =>
      val comment = operation.get(CommentId(cid3.value + 999))
      comment shouldBe None
    }
  }

  describe("create") {
    it("should create a comment, and notify to author when other user commented") { implicit session =>
      val commenter1 = uid2
      val author = uid1
      val targetArticle = Article.findById(aid1).get

      val params1 = Params(Map(
        "user_id" -> commenter1,
        "article_id" -> targetArticle.articleId,
        "body" -> "commentX"
      ))
      val permittedParameters1 = params1.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId,
        "body" -> ParamType.String
      ): _*)
      val comment1 = operation.create(targetArticle, permittedParameters1)
      comment1.body should equal("commentX")

      Article.findById(aid1).foreach(_.commentsCount should equal(4))

      val notificationToAuthor1 = Notification.findBy(sqls.eq(Notification.column.userId, author)).get
      notificationToAuthor1.articleId should equal(targetArticle.articleId)
      notificationToAuthor1.fragmentId should equal(Some(comment1.commentId.value))
      notificationToAuthor1.senderId should equal(commenter1)
      notificationToAuthor1.`type` should equal(NotificationType.Comment.value)

      // --------
      val commenter2 = uid3

      val params2 = Params(Map(
        "user_id" -> commenter2,
        "article_id" -> targetArticle.articleId,
        "body" -> "commentY"
      ))
      val permittedParameters2 = params2.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "article_id" -> IdParamType.ArticleId,
        "body" -> ParamType.String
      ): _*)
      val comment2 = operation.create(targetArticle, permittedParameters2)
      comment2.body should equal("commentY")

      Article.findById(aid1).foreach(_.commentsCount should equal(5))

      val notificationToAuthor2 = Notification.findBy(sqls.eq(Notification.column.userId, author).orderBy(Notification.column.notificationId.desc).limit(1)).get
      notificationToAuthor2.articleId should equal(targetArticle.articleId)
      notificationToAuthor2.fragmentId should equal(Some(comment2.commentId.value))
      notificationToAuthor2.senderId should equal(commenter2)
      notificationToAuthor2.`type` should equal(NotificationType.Comment.value)

      // reply
      val notificationToCommenter = Notification.findBy(sqls.eq(Notification.column.userId, commenter1)).get
      notificationToCommenter.articleId should equal(targetArticle.articleId)
      notificationToCommenter.fragmentId should equal(Some(comment2.commentId.value))
      notificationToCommenter.senderId should equal(commenter2)
      notificationToCommenter.`type` should equal(NotificationType.Reply.value)
    }
  }

  describe("update") {
    it("should update specific comment") { implicit session =>
      val origin = Comment.findById(cid3).get
      val params = Params(Map(
        "body" -> "comment3 updated"
      ))
      val permittedParameters = params.permit(Seq(
        "body" -> ParamType.String
      ): _*)
      val updated = operation.update(origin, permittedParameters)
      updated.body should equal("comment3 updated")

      val history = CommentHistory.findBy(sqls.eq(CommentHistory.column.commentId, cid3)).get
      history.oldBody.get should equal("comment3")
      history.newBody.get should equal("comment3 updated")
      history.deleted shouldBe false
    }
  }

  describe("delete") {
    it("should delete specific comment") { implicit session =>
      val count = operation.delete(User.findById(uid2).get, cid3)
      count should equal(2)
      Article.findById(aid1).get.commentsCount should equal(2)

      val history = CommentHistory.findBy(sqls.eq(CommentHistory.column.commentId, cid3)).get
      history.oldBody should equal(Some("comment3"))
      history.newBody shouldBe None
      history.deleted shouldBe true
    }

    it("should not delete other user's comment") { implicit session =>
      val count = operation.delete(User.findById(uid1).get, cid3)
      count should equal(3)
      Article.findById(aid1).get.commentsCount should equal(3)

      val history = CommentHistory.findBy(sqls.eq(CommentHistory.column.commentId, cid3))
      history shouldBe None
    }
  }

}