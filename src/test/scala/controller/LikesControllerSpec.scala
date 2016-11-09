package controller

import lib.SessionAttribute
import model._
import model.typebinder.{ ArticleId, LikeId }
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }

class LikesControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new LikesController with MockController

  describe("like") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams("article_id" -> "1")
      controller.like

      Like.countByArticleId(ArticleId(1)) should equal(1)
      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      controller.prepareParams() // no parameters
      controller.like

      controller.status should equal(400)
    }
  }

  describe("unlike") {
    it("should unlike target article") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Like).withVariables('likeId -> 1, 'userId -> 1, 'articleId -> 1).create()

      controller.prepareParams("article_id" -> "1")
      controller.unlike

      Like.findById(LikeId(1)) shouldBe None
      controller.status should equal(200)
    }
  }

}
