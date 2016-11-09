package controller

import lib.SessionAttribute
import model._
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController }
import view_model.SearchResult

class SearchControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new SearchController with MockController

  describe("search") {
    it("renders search result") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      for (i <- 1 to 30) {
        FactoryGirl(Article).withVariables('articleId -> i).create()
        FactoryGirl(Tag).withVariables('tagId -> i).withAttributes('name -> ("title" + i), 'taggingsCount -> i).create()
      }

      controller.search("title1")

      val result = controller.getFromRequestScope[SearchResult]("result").get
      result.q should equal("title1")
      result.tag.get.name should equal("title1")
      result.tag.get.taggingsCount should equal(Some(1))
      result.articles.currentPage should equal(1)
      result.articles.totalCount should equal(11)
      result.articles.totalPages should equal(1)
      result.articles.data should have size 11

      renderPath should equal("/search/result")
      okHtmlResponse
    }
  }

}
