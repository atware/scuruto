package controller

import lib.SessionAttribute
import model._
import model.typebinder.UserId
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController, MockHaltException }
import view_model.{ Pagination, UserWithStats }

class UsersControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new UsersController with MockController

  describe("index") {
    it("renders list of a specific user's article list") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      FactoryGirl(User).withVariables('userId -> 2).create()
      for (id <- 2 to 22) {
        FactoryGirl(Article).withVariables('articleId -> id).withAttributes('userId -> 2).create()
      }

      controller.index(UserId(2))

      val user = controller.getFromRequestScope[UserWithStats]("user").get
      user.profile.userId.value should equal(2)
      val items1 = controller.getFromRequestScope[Seq[Article]]("items").get
      items1 should have size 20
      val pagination1 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination1.currentPage should equal(1)
      pagination1.totalCount should equal(21)
      pagination1.totalPages should equal(2)

      renderPath should equal("/users/show")
      okHtmlResponse

      // --------
      controller.prepareParams("page" -> "2")
      controller.index(UserId(2))
      val items2 = controller.getFromRequestScope[Seq[Article]]("items").get
      items2 should have size 1
      val pagination2 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination2.currentPage should equal(2)
      pagination2.totalCount should equal(21)
      pagination2.totalPages should equal(2)

      renderPath should equal("/users/show")
      okHtmlResponse
    }

    it("should be 404 with unknown user") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      try {
        controller.index(UserId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(404))
        case _: Throwable => fail()
      }
    }
  }

  describe("stocks") {
    it("renders stock list") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      FactoryGirl(User).withVariables('userId -> 2).create()
      for (id <- 1 to 21) {
        FactoryGirl(Article).withVariables('articleId -> id).withAttributes('userId -> 2).create()
        FactoryGirl(Stock).withVariables('stockId -> id, 'userId -> 1, 'articleId -> id).create()
      }

      controller.stocks(1)

      val items1 = controller.getFromRequestScope[Seq[Article]]("items").get
      items1 should have size 20
      val pagination1 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination1.currentPage should equal(1)
      pagination1.totalCount should equal(21)
      pagination1.totalPages should equal(2)

      renderPath should equal("/users/stocks")
      okHtmlResponse

      // --------
      controller.prepareParams("page" -> "2")
      controller.stocks(2)
      val items2 = controller.getFromRequestScope[Seq[Article]]("items").get
      items2 should have size 1
      val pagination2 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination2.currentPage should equal(2)
      pagination2.totalCount should equal(21)
      pagination2.totalPages should equal(2)

      renderPath should equal("/users/stocks")
      okHtmlResponse
    }
  }

  describe("edit") {
    it("should render profile edit page") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      controller.edit(UserId(1))

      renderPath should equal("/users/edit")
      okHtmlResponse
    }

    it("should be 403 with other user's id") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      FactoryGirl(User).withVariables('userId -> 2).create()

      try {
        controller.edit(UserId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(403))
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
        "name" -> "user name",
        "imageUrl" -> "/foo/bar.png",
        "comment" -> "new comment",
        "locale" -> "xx"
      )
      controller.update(UserId(1))

      sessionValue[String]("locale") should equal(Some("xx"))
      val user: User = sessionValue[User](SessionAttribute.LoginUser.key).get
      user.comment should equal(Some("new comment"))
      user.name should equal("user name")
      user.imageUrl should equal(Some("/foo/bar.png"))
      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams() // no parameters
      controller.update(UserId(1))

      controller.status should equal(400)
    }

    it("should be 403 with other user's id") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      FactoryGirl(User).withVariables('userId -> 2).create()

      try {
        controller.edit(UserId(2))
      } catch {
        case e: MockHaltException => e.status should equal(Some(403))
        case _: Throwable => fail()
      }
    }
  }

}
