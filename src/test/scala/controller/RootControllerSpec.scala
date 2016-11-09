package controller

import lib.SessionAttribute
import model._
import model.typebinder.ArticleId
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }
import view_model.IndexSidebar

class RootControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new RootController with MockController

  describe("index") {
    it("set current locale to 'en' and render login page when not logged in") {
      implicit val controller = createMockController
      initSession

      controller.index

      sessionValue[String]("locale") should equal(Some("en"))

      renderPath should equal("/login")
      okHtmlResponse
    }

    it("should render article list page when logged in") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      for (i <- 2 to 10) {
        FactoryGirl(User).withVariables('userId -> i).create()
      }
      for (i <- 1 to 25) {
        FactoryGirl(Article).withVariables('articleId -> i).withAttributes('stocksCount -> i).create()
      }
      FactoryGirl(Stock).withVariables('stockId -> 1, 'userId -> 2, 'articleId -> 1).create()
      FactoryGirl(Like).withVariables('likeId -> 1, 'userId -> 3, 'articleId -> 2).create()

      controller.index

      val items = controller.getFromRequestScope[Seq[Article]]("items").get
      items should have size 20
      items.head.articleId.value should equal(25)
      items.last.articleId.value should equal(6)

      val sidebar = controller.getFromRequestScope[IndexSidebar]("sidebar").get
      sidebar.contribution should equal(4) // user1: 3(stock)+1(like)
      sidebar.populars should have size 5
      sidebar.contributors should have size 1 // user1 only

      renderPath should equal("/articles/list")
      okHtmlResponse
    }
  }

  describe("more") {
    it("should return next page html") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      for (i <- 2 to 10) {
        FactoryGirl(User).withVariables('userId -> i).create()
      }
      for (i <- 1 to 25) {
        FactoryGirl(Article).withVariables('articleId -> i).create()
      }

      controller.more(ArticleId(6))

      val items = controller.getFromRequestScope[Seq[Article]]("items").get
      items should have size 5
      items.head.articleId.value should equal(5)
      items.last.articleId.value should equal(1)

      renderPath should equal("/articles/scroll")
      okHtmlResponse
    }
  }

}
