package controller

import lib.SessionAttribute
import model._
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }

class NotificationsControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new NotificationsController with MockController

  describe("index") {
    it("returns notification") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      FactoryGirl(User).withVariables('userId -> 2).create()
      for (id <- 1 to 16) {
        FactoryGirl(Notification).withVariables('notificationId -> id, 'userId -> 1, 'articleId -> 1, 'senderId -> 2).create()
      }

      val response = controller.index

      controller.status should equal(200)
      response should startWith("{\"count\":15")
      response.split("\\{\"").filter(_.startsWith("image")) should have size 15
    }
  }

  describe("state") {
    it("updates notification state and returns json of count zero") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      FactoryGirl(User).withVariables('userId -> 2).create()
      for (id <- 1 to 16) {
        FactoryGirl(Notification).withVariables('notificationId -> id, 'userId -> 1, 'articleId -> 1, 'senderId -> 2).create()
      }

      val response = controller.state

      controller.status should equal(200)
      Notification.findAll().foreach { _.state shouldBe true }
      response should equal("{\"count\":0,\"data\":[]}")
    }
  }

}
