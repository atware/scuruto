package controller

import _root_.validator.validChar
import integration.ExternalServiceIntegration
import lib.Markdown
import model.Comment
import model.typebinder.{ ArticleId, CommentId }
import operation._
import skinny._
import skinny.validator._

class CommentsController extends ApplicationController {
  protectFromForgery()

  // --------------
  // POST /comments
  def createParams: Params = {
    loginUser match {
      case Some(user) => Params(params ++ Map("user_id" -> user.userId))
      case _ => Params(params.filterKeys(key => key != "user_id"))
    }
  }

  def createForm: MapValidator = validation(
    createParams,
    paramKey("user_id") is required,
    paramKey("body") is required & validChar,
    paramKey("article_id") is required
  )

  def createFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "user_id" -> IdParamType.UserId,
    "article_id" -> IdParamType.ArticleId,
    "body" -> ParamType.String
  )

  def create = {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val articleId = ArticleId(params.getAs[Long]("article_id").get)
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      articleOperation.get(articleId).map { article =>
        val commentOperation: CommentOperation = inject[CommentOperation]
        val permittedParameters = createParams.permit(createFormStrongParameters: _*)
        debugLoggingPermittedParameters(permittedParameters)
        val comment = commentOperation.create(article, permittedParameters)

        // Notify to external service
        val externalServiceIntegration = inject[ExternalServiceIntegration]
        externalServiceIntegration.onCommentCreated(loginUser.get, comment)

        //flash += ("notice" -> createI18n().get("comment.flash.created").getOrElse("The comment was created."))
        redirect303(s"/articles/${comment.articleId}#comment-${comment.commentId}")
      } getOrElse haltWithBody(404)
    } else {
      val articleId = params.getAsOrElse[Long]("article_id", -1L)
      redirect302(s"/articles/$articleId")
    }
  }

  // --------------
  // POST /comments/:id
  def updateParams: Params = Params(params)
  def updateForm: MapValidator = validation(
    updateParams,
    paramKey("body") is required & validChar
  )
  def updateFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "body" -> ParamType.String
  )
  def update(id: CommentId) = {
    debugLoggingParameters(updateForm, Some(id.value))
    val commentOperation: CommentOperation = inject[CommentOperation]
    getOwnComment(id) match {
      case Some(comment) => {
        if (updateForm.validate()) {
          val permittedParameters = updateParams.permit(updateFormStrongParameters: _*)
          debugLoggingPermittedParameters(permittedParameters, Some(id.value))
          val updated = commentOperation.update(comment, permittedParameters)
          toJSONString(Map(
            "raw" -> updated.body,
            "html" -> Markdown.toHtml(updated.body)
          ))
        } else {
          status = 400
          ""
        }
      }
      case _ => status = 403
    }
  }

  // --------------
  // DELETE /comments/:id
  def delete(id: CommentId) = {
    val commentOperation: CommentOperation = inject[CommentOperation]
    val counter = commentOperation.delete(loginUser.get, id)
    status = 200
    counter
  }

  // --------------
  private def getOwnComment(id: CommentId): Option[Comment] = {
    loginUser.flatMap { u =>
      val commentOperation = inject[CommentOperation]
      commentOperation.get(id).filter(c => c.userId == u.userId)
    }
  }

}
