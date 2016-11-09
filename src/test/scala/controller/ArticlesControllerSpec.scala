package controller

import lib.SessionAttribute
import model._
import model.typebinder.ArticleId
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController, MockHaltException }
import view_model.{ ArticleReaction, ArticleSidebar }

class ArticlesControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new ArticlesController with MockController

  describe("index") {
    it("should render article page") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 2, 'articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 2, 'userId -> 1, 'articleId -> 1).create()

      controller.index(ArticleId(1))

      val item = controller.getFromRequestScope[Article]("item").get
      item.articleId.value should equal(1)
      item.title should equal("article title1")
      item.body should equal("article body1")
      val comments = controller.getFromRequestScope[Seq[Comment]]("comments").get
      comments should have size 2
      val reaction = controller.getFromRequestScope[ArticleReaction]("reaction").get
      reaction.stockable shouldBe true
      reaction.likeable shouldBe true
      reaction.stockedUsers should have size 0
      val sidebar = controller.getFromRequestScope[ArticleSidebar]("sidebar").get
      sidebar.contribution should equal(0)

      renderPath should equal("/articles/show")
      okHtmlResponse
    }
  }

  describe("input") {
    it("should render input page") {
      implicit val controller = createMockController

      controller.input

      val item = controller.getFromRequestScope[Article]("item").get
      item.title should equal("")
      item.body should equal("")
      item.tags should have size 0

      renderPath should equal("/articles/new")
      okHtmlResponse
    }
  }

  describe("create") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      controller.prepareParams(
        "title" -> "new title",
        "body" -> "new body",
        "tags" -> "tag1",
        "tags" -> "tag2"
      )
      controller.create

      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      controller.prepareParams() // no parameters
      controller.create

      val item = controller.getFromRequestScope[Article]("item").get
      item.title should equal("")
      item.body should equal("")
      item.tags should have size 0

      controller.status should equal(400)
    }
  }

  describe("edit") {
    it("should render edit page with own article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.edit(ArticleId(1))

      val item = controller.getFromRequestScope[Article]("item").get
      item.title should equal("article title1")
      item.body should equal("article body1")
      item.tags should have size 0

      renderPath should equal("/articles/edit")
      okHtmlResponse
    }

    it("should be 404 with other user's article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 2).withAttributes('userId -> 2).create()

      try {
        controller.edit(ArticleId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(404))
        case _: Throwable => fail()
      }
    }
  }

  describe("update") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams(
        "title" -> "new title",
        "body" -> "new body",
        "tags" -> "new tag1",
        "tags" -> "new tag2"
      )
      controller.update(ArticleId(1))

      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams( // no body parameter
        "title" -> "new title",
        "tags" -> "new tag1"
      )
      controller.update(ArticleId(1))

      val item = controller.getFromRequestScope[Article]("item").get
      item.title should equal("new title")
      item.body should equal("")
      item.tags should have size 1

      controller.status should equal(400)
    }

    it("fails with illegal chars") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams(
        "title" -> "new title",
        "body" -> "new body",
        "tags" -> "new tag1",
        "tags" -> "\uD83C\uDF5C"
      )
      controller.update(ArticleId(1))

      val item = controller.getFromRequestScope[Article]("item").get
      item.tags should have size 2
      item.tags.last.name should equal("\uD83C\uDF5C")
      controller.status should equal(400)
    }

    it("should be 404 with other user's article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 2).withAttributes('userId -> 2).create()

      try {
        controller.edit(ArticleId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(404))
        case _: Throwable => fail()
      }
    }
  }

  describe("stockers") {
    it("should return stockers") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(User).withVariables('userId -> 3).create()
      FactoryGirl(User).withVariables('userId -> 4).create()
      FactoryGirl(Stock).withVariables('stockId -> 1, 'userId -> 2, 'articleId -> 1).create()
      FactoryGirl(Stock).withVariables('stockId -> 2, 'userId -> 3, 'articleId -> 1).create()
      FactoryGirl(Stock).withVariables('stockId -> 3, 'userId -> 4, 'articleId -> 1).create()

      controller.stockers(ArticleId(1))

      val item = controller.getFromRequestScope[Article]("item").get
      item.articleId.value should equal(1)
      val stockers = controller.getFromRequestScope[Seq[User]]("stockers").get
      stockers should have size 3
      stockers.head.userId.value should equal(4)
      stockers.last.userId.value should equal(2)

      renderPath should equal("/articles/stockers")
      okHtmlResponse
    }

    it("should be 404 with non exists article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      try {
        controller.stockers(ArticleId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(404))
        case _: Throwable => fail()
      }
    }
  }

}
