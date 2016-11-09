package controller

import model._
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }

class ApiControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new ApiController with MockController

  describe("preview") {
    it("converts markdown to html") {
      implicit val controller = createMockController
      controller.prepareParams("body" -> "markdown")
      val response = controller.preview
      controller.status should equal(200)
      response should equal("{\"html\":\"<p>markdown</p>\"}")
    }
  }

  describe("tags") {
    it("returns tag name prefix search results") {
      implicit val controller = createMockController

      FactoryGirl(Tag).withVariables('tagId -> 1).withAttributes('name -> "java").create()
      FactoryGirl(Tag).withVariables('tagId -> 2).withAttributes('name -> "scala").create()
      FactoryGirl(Tag).withVariables('tagId -> 3).withAttributes('name -> "javascript").create()

      controller.prepareParams("q" -> "java")
      val response = controller.tags
      controller.status should equal(200)
      response should equal("[\"java\",\"javascript\"]")
    }
  }

}
