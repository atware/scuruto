package controller

import java.net.URLEncoder

import _root_.validator._
import integration.ExternalServiceIntegration
import model._
import model.typebinder.ArticleId
import operation._
import skinny._
import skinny.validator._

/**
 * The controller for /articles endpoints.
 */
class ArticlesController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /articles/{id}
  def index(id: ArticleId) = {
    loginUser.map { u =>
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      val commentOperation: CommentOperation = inject[CommentOperation]
      articleOperation.getWithTag(id).map { article =>
        set("item", article)
        set("comments", commentOperation.getAll(article.articleId))
        set("reaction", articleOperation.getReaction(article, loginUser.get))
        set("sidebar", articleOperation.getArticleSidebar(article))
        render(s"/articles/show")
      } getOrElse haltWithBody(404)
    } getOrElse {
      val isOgp = params.get("o").isDefined
      if (isOgp) {
        val ogpOperation = inject[OgpOperation]
        ogpOperation.buildOgp(id).map { ogp =>
          set("ogp", ogp)
          render(s"/ogp/index")
        } getOrElse redirect302("/?ref=" + URLEncoder.encode("/articles/" + id, "UTF-8"))
      } else {
        redirect302("/?ref=" + getRef)
      }
    }
  }

  // --------------
  // GET /articles/new
  def input = {
    set("item", Article.emptyModel())
    render(s"/articles/new")
  }

  // --------------
  // POST /articles
  def createParams: Params = {
    val title = params.getOrElse("title", "")
    val body = params.getOrElse("body", "")
    val tags = multiParams.get("tags").getOrElse(Seq()).map(_.trim).distinct.filter(_.nonEmpty)
    val paramsMap = Map("title" -> title, "body" -> body, "tags" -> tags)
    loginUser.map { u =>
      Params(paramsMap ++ Map("user_id" -> u.userId))
    } getOrElse Params(paramsMap)
  }
  def createForm: MapValidator = validation(
    createParams,
    paramKey("user_id") is required,
    paramKey("title") is required & validChar & maxLength(100),
    paramKey("body") is required & validChar,
    paramKey("tags") is validChars & maxLengths(30)
  )
  def createFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "user_id" -> IdParamType.UserId,
    "title" -> ParamType.String,
    "body" -> ParamType.String
  )
  def create = {
    debugLoggingParameters(createForm)
    if (createForm.validate()) {
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      val permittedParameters = createParams.permit(createFormStrongParameters: _*)
      debugLoggingPermittedParameters(permittedParameters)
      val article = articleOperation.create(permittedParameters, updateParams.getAs[Seq[String]]("tags").get)

      // Notify to external service
      val externalServiceIntegration = inject[ExternalServiceIntegration]
      externalServiceIntegration.onPostCreated(loginUser.get, article)

      //flash += ("notice" -> createI18n().get("article.flash.created").getOrElse("The article was created."))
      redirect303(s"/articles/${article.articleId}")
    } else {
      status = 400
      val article: Article = Article.fromParams(params, multiParams)
      set("item", article)
      set("loginUser" -> loginUser)
      resetCsrf
      render("/articles/new")
    }
  }

  // --------------
  // GET /articles/{id}/edit
  def edit(id: ArticleId) = {
    getOwnArticle(id).map { article =>
      set("item", article)
      render(s"/articles/edit")
    } getOrElse haltWithBody(404)
  }

  def updateParams: Params = {
    val title = params.getOrElse("title", "")
    val body = params.getOrElse("body", "")
    val tags = multiParams.get("tags").getOrElse(Seq()).map(_.trim).distinct.filter(_.nonEmpty)
    Params(Map("title" -> title, "body" -> body, "tags" -> tags))
  }
  def updateForm: MapValidator = validation(
    updateParams,
    paramKey("title") is required & validChar & maxLength(100),
    paramKey("body") is required & validChar,
    paramKey("tags") is validChars & maxLengths(30)
  )
  def updateFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "title" -> ParamType.String,
    "body" -> ParamType.String
  )

  // --------------
  // POST /articles/{id}
  def update(id: ArticleId) = {
    debugLoggingParameters(updateForm, Some(id.value))
    getOwnArticle(id).map { article =>
      if (updateForm.validate()) {
        val articleOperation: ArticleOperation = inject[ArticleOperation]
        val permittedParameters = updateParams.permit(updateFormStrongParameters: _*)
        debugLoggingPermittedParameters(permittedParameters, Some(id.value))
        articleOperation.update(article, permittedParameters, updateParams.getAs[Seq[String]]("tags").get)

        //flash += ("notice" -> createI18n().get("article.flash.updated").getOrElse("The article was updated."))
        redirect302(s"/articles/$id")
      } else {
        status = 400
        val article: Article = Article.fromParams(params, multiParams, id)
        set("item", article)
        resetCsrf
        render(s"/articles/edit")
      }
    } getOrElse haltWithBody(404)
  }

  // --------------
  // DELETE /articles/{id}
  /*
  def destroyResource(id: Long) = {
    getOwnArticle.map { _ => // article found
      Article.deleteById(id)
      status = 200
    } getOrElse haltWithBody(404)
  }
  */

  // --------------
  // GET /articles/{id}/stockers
  def stockers(id: ArticleId) = {
    val articleOperation: ArticleOperation = inject[ArticleOperation]
    val stockOperation: StockOperation = inject[StockOperation]
    articleOperation.getWithTag(id).map { article =>
      set("item", article)
      set("stockers", stockOperation.getStockers(article.articleId))
      render("/articles/stockers")
    } getOrElse haltWithBody(404)
  }

  // --------------
  private def getOwnArticle(id: ArticleId): Option[Article] = {
    loginUser.map { u =>
      val articleOperation: ArticleOperation = inject[ArticleOperation]
      articleOperation.getWithTag(id).filter(a => a.userId == u.userId)
    } getOrElse None
  }

}