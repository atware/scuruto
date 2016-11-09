package operation

import controller.IdParamType
import model._
import model.typebinder.{ ArticleId, TagId, UserId }
import org.scalatest.{ Matchers, fixture }
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import skinny.ParamType
import skinny.controller.Params

class ArticleOperationSpec extends fixture.FunSpec with AutoRollback with Matchers with TestDBSettings {
  val operation = new ArticleOperationImpl

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
    aid6 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title6", a.body -> "body6", a.stocksCount -> 1)
    val aid7 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title7", a.body -> "body7", a.stocksCount -> 0)
    aid8 = Article.createWithNamedValues(a.userId -> uid3, a.title -> "title8", a.body -> "body8", a.stocksCount -> 2)
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
    ArticlesTags.createWithNamedValues(at.articleId -> aid11, at.tagId -> tid3)
    ArticlesTags.createWithNamedValues(at.articleId -> aid11, at.tagId -> tid1)

    val s = Stock.column
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid4)
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid6)
    Stock.createWithNamedValues(s.userId -> uid1, s.articleId -> aid8)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid8)
    Stock.createWithNamedValues(s.userId -> uid2, s.articleId -> aid2)
    Stock.createWithNamedValues(s.userId -> uid3, s.articleId -> aid2)

    val l = Like.column
    Like.createWithNamedValues(l.userId -> uid2, l.articleId -> aid2)
    Like.createWithNamedValues(l.userId -> uid1, l.articleId -> aid2)
    Like.createWithNamedValues(l.userId -> uid3, l.articleId -> aid2)
  }

  describe("get") {
    it("should return a expected article") { implicit session =>
      operation.get(aid2).map { a =>
        a.articleId should equal(aid2)
        a.userId should equal(uid1)
        a.title should equal("title2")
        a.body should equal("body2")
        a.stocksCount should equal(2)
      } getOrElse fail()
    }
  }

  describe("getPage") {
    it("should return first page") { implicit session =>
      val page = operation.getPage(pageSize = 10)
      page should have size 10
      page.head.title should equal("title11")
      page.last.title should equal("title2")
    }
    it("should return second page") { implicit session =>
      val page = operation.getPage(maxId = aid2, pageSize = 10)
      page should have size 1
      page.head.title should equal("title1")
    }
  }

  describe("getWithTag") {
    it("should return a article with tags") { implicit session =>
      operation.getWithTag(aid11).map { article =>
        article.title should equal("title11")
        article.tags should have size 3
        article.tags.head.name should equal("tag2")
        article.tags.last.name should equal("tag1")
      } getOrElse fail()
    }
  }

  describe("getReaction") {
    it("should return reaction - stockable, likeable") { implicit session =>
      val reaction = operation.getReaction(Article.findById(aid4).get, User.findById(uid2).get)
      reaction.stockable shouldBe true
      reaction.likeable shouldBe true
      reaction.stockedUsers should have size 1
      reaction.stockedUsers.head.userId should equal(uid1)
    }
    it("should return reaction - unstockable, unlikeable") { implicit session =>
      val reaction = operation.getReaction(Article.findById(aid2).get, User.findById(uid2).get)
      reaction.stockable shouldBe false
      reaction.likeable shouldBe false
      reaction.stockedUsers should have size 2
      reaction.stockedUsers.head.userId should equal(uid3)
      reaction.stockedUsers.last.userId should equal(uid2)
    }
  }

  describe("getIndexSidebar") {
    it("should return sidebar model for top page") { implicit session =>
      val sidebar = operation.getIndexSidebar(User.findById(uid1).get)

      sidebar.contribution should equal(8)

      val populars: Seq[Article] = sidebar.populars
      populars should have size 4
      populars.head.articleId should equal(aid8)
      populars(1).articleId should equal(aid2)
      populars(2).articleId should equal(aid6)
      populars.last.articleId should equal(aid4)
      // should not contain 0 stock article

      val contributors: Seq[Map[String, Any]] = sidebar.contributors
      contributors should have size 2
      contributors.head.get("uid") should equal(Some(uid3.value))
      contributors.last.get("uid") should equal(Some(uid1.value))
      // should not contain 0 contribution user
    }
  }

  describe("getArticleSidebar") {
    it("should return sidebar model for article page") { implicit session =>
      val sidebar = operation.getArticleSidebar(Article.findById(aid2).get)
      sidebar.contribution should equal(8)
    }
  }

  describe("getPageByTag") {
    it("should return first page by tag") { implicit session =>
      val page = operation.getPageByTag(Tag.findById(tid2).get, pageNo = 1, pageSize = 3)
      page.totalCount should equal(4)
      page.totalPages should equal(2)
      page.data should have size 3
      page.data.head.title should equal("title11")
      page.data.last.title should equal("title5")
    }
    it("should return second page by tag") { implicit session =>
      val page = operation.getPageByTag(Tag.findById(tid2).get, pageNo = 2, pageSize = 3)
      page.totalCount should equal(4)
      page.totalPages should equal(2)
      page.data should have size 1
      page.data.head.title should equal("title2")
    }
  }

  describe("getPageByUser") {
    it("should return first page by user") { implicit session =>
      val page = operation.getPageByUser(User.findById(uid3).get, pageNo = 1, pageSize = 4)
      page.totalCount should equal(10)
      page.totalPages should equal(3)
      page.data should have size 4
      page.data.head.title should equal("title11")
      page.data.last.title should equal("title8")
    }
    it("should return last page by user") { implicit session =>
      val page = operation.getPageByUser(User.findById(uid3).get, pageNo = 3, pageSize = 4)
      page.totalCount should equal(10)
      page.totalPages should equal(3)
      page.data should have size 2
      page.data.head.title should equal("title3")
      page.data.last.title should equal("title1")
    }
  }

  describe("getPageByUserStocked") {
    it("should return first page user stocked") { implicit session =>
      val page = operation.getPageByUserStocked(User.findById(uid1).get, pageNo = 1, pageSize = 2)
      page.totalCount should equal(3)
      page.totalPages should equal(2)
      page.data should have size 2
      page.data.head.title should equal("title8")
      page.data.last.title should equal("title6")
    }
    it("should return last page user stocked") { implicit session =>
      val page = operation.getPageByUserStocked(User.findById(uid1).get, pageNo = 2, pageSize = 2)
      page.totalCount should equal(3)
      page.totalPages should equal(2)
      page.data should have size 1
      page.data.head.title should equal("title4")
    }
  }

  describe("create") {
    it("should create a article and tags") { implicit session =>
      val params = Params(Map(
        "user_id" -> uid3,
        "title" -> "titleX",
        "body" -> "bodyX"
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "title" -> ParamType.String,
        "body" -> ParamType.String
      ): _*)
      val article = operation.create(permittedParameters, Seq("tag1", "tagNew"))
      article.title should equal("titleX")
      article.tags should have size 2

      Tag.findByName("tag1").get.taggingsCount should equal(Some(6))
      Tag.findByName("tagNew").get.taggingsCount should equal(Some(1))
    }
  }

  describe("update") {
    it("should update specific article and tags then save history") { implicit session =>
      val origin = Article.joins(Article.tagsRef).findById(aid11.value).get
      val params = Params(Map(
        "user_id" -> uid3,
        "title" -> "titleX",
        "body" -> "bodyX"
      ))
      val permittedParameters = params.permit(Seq(
        "user_id" -> IdParamType.UserId,
        "title" -> ParamType.String,
        "body" -> ParamType.String
      ): _*)
      val article = operation.update(origin, permittedParameters, Seq("tag1", "tagNew"))
      article.title should equal("titleX")
      article.tags should have size 2

      Tag.findByName("tag1").get.taggingsCount should equal(Some(5))
      Tag.findByName("tagNew").get.taggingsCount should equal(Some(1))

      val history = UpdateHistory.findBy(sqls.eq(Article.column.articleId, aid11)).get
      history.oldTitle should equal("title11")
      history.oldBody should equal("body11")
      history.oldTags should equal(Some("tag2,tag3,tag1"))
      history.newTitle should equal("titleX")
      history.newBody should equal("bodyX")
      history.newTags should equal(Some("tag1,tagNew"))
    }
  }

}