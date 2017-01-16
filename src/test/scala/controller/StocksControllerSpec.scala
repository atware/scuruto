package controller

import lib.SessionAttribute
import model._
import model.typebinder.ArticleId
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }

class StocksControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new StocksController with MockController

  describe("stock") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams("article_id" -> "1")
      controller.stock

      Stock.countByArticleId(ArticleId(1)) should equal(1)
      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      controller.prepareParams() // no parameters
      controller.stock

      controller.status should equal(400)
    }
  }

  describe("unstock") {
    it("should unstock target article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Stock).withVariables('stockId -> 1, 'userId -> 1, 'articleId -> 1).create()

      controller.prepareParams("article_id" -> "1")
      controller.unstock

      Stock.findById(1) shouldBe None
      controller.status should equal(200)
    }
  }

  describe("unstock ignore") {
    it("should ignore unstock when not stocked") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams("article_id" -> "1")
      controller.unstock

      Stock.findById(1) shouldBe None
      controller.status should equal(200)
    }
  }

}
