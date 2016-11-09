package controller

import model.typebinder.ArticleId
import operation._
import skinny._
import skinny.validator._

class LikesController extends ApplicationController {
  protectFromForgery()

  // --------------
  // POST /likes
  def createParams: Params = {
    loginUser.map { u =>
      Params(params ++ Map("user_id" -> u.userId))
    } getOrElse Params(params.filterKeys(key => key != "user_id"))
  }
  def createForm: MapValidator = validation(
    createParams,
    paramKey("user_id") is required,
    paramKey("article_id") is required
  )
  def createFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "user_id" -> IdParamType.UserId,
    "article_id" -> IdParamType.ArticleId
  )
  def like = {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val articleId = ArticleId(params.getAs[Long]("article_id").get)
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      articleOperation.get(articleId).map { article =>
        val likeOperation: LikeOperation = inject[LikeOperation]
        val permittedParameters = createParams.permit(createFormStrongParameters: _*)
        debugLoggingPermittedParameters(permittedParameters)
        if (!likeOperation.exists(permittedParameters)) {
          val counter = likeOperation.like(article, permittedParameters)
          status = 200
          counter
        } else {
          status = 400
        }
      } getOrElse (status = 400)
    } else {
      status = 400
    }
  }

  // --------------
  // DELETE /likes
  def unlike = {
    val likeOperation: LikeOperation = inject[LikeOperation]
    val articleId = ArticleId(params.getAsOrElse[Long]("article_id", -1L))
    def counter = likeOperation.unlike(loginUser.get, articleId)
    status = 200
    counter
  }

}
