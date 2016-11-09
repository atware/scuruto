package controller

import lib.SessionAttribute
import model._
import model.typebinder.CommentId
import org.scalatest.FunSpec
import skinny.test.{ FactoryGirl, MockController, MockHaltException }

class CommentsControllerSpec extends FunSpec with ControllerSpec with TestDBSettings {

  def createMockController = new CommentsController with MockController

  describe("create") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()

      controller.prepareParams(
        "body" -> "new comment",
        "article_id" -> "1"
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

      controller.status should equal(400)
    }
  }

  describe("update") {
    it("succeeds with valid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 1, 'articleId -> 1).create()

      controller.prepareParams(
        "body" -> "update comment"
      )
      controller.update(CommentId(1))

      controller.status should equal(200)
    }

    it("fails with invalid parameters") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 1, 'articleId -> 1).create()

      controller.prepareParams( // empty body parameter
        "body" -> ""
      )
      controller.update(CommentId(1))

      controller.status should equal(400)
    }

    it("fails with illegal chars") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 1, 'articleId -> 1).create()

      controller.prepareParams(
        "body" -> "\uD83C\uDF5C"
      )
      controller.update(CommentId(1))

      controller.status should equal(400)
    }

    it("should be 403 with other user's comment") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 2).withAttributes('userId -> 2).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 2, 'articleId -> 1).create()

      try {
        controller.update(CommentId(1))
      } catch {
        case e: MockHaltException => e.status should equal(Some(403))
        case _: Throwable => fail()
      }
    }
  }

  describe("delete") {
    it("deletes specific comment") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 1, 'articleId -> 1).create()

      val count = controller.delete(CommentId(1))

      controller.status should equal(200)
      count should equal(0)
    }
    it("not deletes other user's comment") {
      implicit val controller = createMockController

      val loginUser = FactoryGirl(User).withVariables('userId -> 1).create()
      prepareSessionValue(SessionAttribute.LoginUser.key, loginUser)
      FactoryGirl(Article).withVariables('articleId -> 1).create()
      FactoryGirl(User).withVariables('userId -> 2).create()
      FactoryGirl(Article).withVariables('articleId -> 2).withAttributes('userId -> 2).create()
      FactoryGirl(Comment).withVariables('commentId -> 1, 'userId -> 2, 'articleId -> 1).create()

      val count = controller.delete(CommentId(1))

      controller.status should equal(200)
      count should equal(1)
    }
  }

}
