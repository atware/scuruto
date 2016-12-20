package controller

import model.typebinder.ArticleId
import operation._
import skinny._
import skinny.validator._

class StocksController extends ApplicationController {
  protectFromForgery()

  // --------------
  // POST /stocks
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
  def stock = {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val articleId = ArticleId(params.getAs[Long]("article_id").get)
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      articleOperation.get(articleId) match {
        case Some(article) => {
          val stockOperation: StockOperation = inject[StockOperation]
          val permittedParameters = createParams.permit(createFormStrongParameters: _*)
          debugLoggingPermittedParameters(permittedParameters)
          if (!stockOperation.exists(permittedParameters)) {
            val counter = stockOperation.stock(article, permittedParameters)
            status = 200
            counter
          } else {
            status = 400
          }
        }
        case _ => status = 400
      }
    } else {
      status = 400
    }
  }

  // --------------
  // DELETE /stocks
  def unstock = {
    val stockOperation: StockOperation = inject[StockOperation]
    val articleId = ArticleId(params.getAsOrElse[Long]("article_id", -1L))
    val counter = stockOperation.unstock(loginUser.get, articleId)
    status = 200
    counter
  }

}
