package controller

import lib.SessionAttribute
import model._
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController, MockHaltException }
import view_model.Pagination

class TagsControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new TagsController with MockController

  describe("index") {
    it("renders article list by tag name") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      FactoryGirl(Tag).withVariables('tagId -> 1).create()
      for (id <- 1 to 21) {
        FactoryGirl(Article).withVariables('articleId -> id).create()
        ArticlesTags.createWithNamedValues(ArticlesTags.column.articleId -> id, ArticlesTags.column.tagId -> 1)
      }
      FactoryGirl(Tag).withVariables('tagId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 22).create()
      ArticlesTags.createWithNamedValues(ArticlesTags.column.articleId -> 22, ArticlesTags.column.tagId -> 2)

      controller.index("tag1")

      val tag = controller.getFromRequestScope[Tag]("tag").get
      tag.name should equal("tag1")
      val items1 = controller.getFromRequestScope[Seq[Article]]("items").get
      items1 should have size 20
      val pagination1 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination1.currentPage should equal(1)
      pagination1.totalCount should equal(21)
      pagination1.totalPages should equal(2)

      renderPath should equal("/tags/list")
      okHtmlResponse

      // --------
      controller.prepareParams("page" -> "2")
      controller.index("tag1")
      val items2 = controller.getFromRequestScope[Seq[Article]]("items").get
      items2 should have size 1
      val pagination2 = controller.getFromRequestScope[Pagination[Article]]("pagination").get
      pagination2.currentPage should equal(2)
      pagination2.totalCount should equal(21)
      pagination2.totalPages should equal(2)

      renderPath should equal("/tags/list")
      okHtmlResponse
    }

    it("should be 404 with unknown tag") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)

      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Tag).withVariables('tagId -> 1).create()
      ArticlesTags.createWithNamedValues(ArticlesTags.column.articleId -> 1, ArticlesTags.column.tagId -> 1)

      try {
        controller.index("unknown tag")
      } catch {
        case e: MockHaltException => e.status should equal(Some(404))
        case _: Throwable => fail()
      }
    }
  }

}
