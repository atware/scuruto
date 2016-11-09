package controller

import _root_.validator.validChar
import lib.SessionAttribute
import model.typebinder.UserId
import operation._
import skinny._
import skinny.validator._

class UsersController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /users/{id}
  def index(id: UserId): String = {
    val userOperation = inject[UserOperation]
    userOperation.getWithStats(id).map { user =>
      val articleOperation = inject[ArticleOperation]
      val pageNo: Int = params.getAs[Int]("page").getOrElse(1)
      val pagination = articleOperation.getPageByUser(user.profile, pageNo)
      set("user", user)
      set("items", pagination.data)
      set("pagination", pagination)
      render(s"/users/show")
    } getOrElse haltWithBody(404)
  }

  // --------------
  // GET /users/{id}/stocks
  def stocks = {
    loginUser.map { u =>
      val articleOperation = inject[ArticleOperation]
      val pageNo: Int = params.getAs[Int]("page").getOrElse(1)
      val pagination = articleOperation.getPageByUserStocked(u, pageNo)
      set("items", pagination.data)
      set("pagination", pagination)
      render(s"/users/stocks")
    } getOrElse haltWithBody(403)
  }

  // --------------
  // GET /users/{id}/edit
  def edit(id: UserId) = {
    if (id == loginUser.get.userId) {
      render(s"/users/edit")
    } else {
      haltWithBody(403)
    }
  }

  // --------------
  // POST /users/{id}
  def updateParams: Params = Params(params)
  def updateForm: MapValidator = validation(
    updateParams,
    paramKey("name") is required & validChar & maxLength(200),
    paramKey("imageUrl") is required,
    paramKey("comment") is validChar & maxLength(200),
    paramKey("locale") is required & maxLength(2)
  )
  def updateFormStrongParameters: Seq[(String, ParamType)] = Seq(
    "name" -> ParamType.String,
    "imageUrl" -> ParamType.String,
    "comment" -> ParamType.String,
    "locale" -> ParamType.String
  )
  def update(id: UserId) = {
    debugLoggingParameters(updateForm, Some(id.value))
    if (id == loginUser.get.userId) {
      if (updateForm.validate()) {
        val userOperation = inject[UserOperation]
        val permittedParameters = updateParams.permit(updateFormStrongParameters: _*)
        debugLoggingPermittedParameters(permittedParameters, Some(id.value))
        val updated = userOperation.update(id, permittedParameters)

        skinnySession.setAttribute(SessionAttribute.LoginUser.key, updated)
        setCurrentLocale(updated.locale)

        redirect302(s"/users/$id/edit")
      } else {
        status = 400
        set("loginUser" -> Option(model.User.fromParams(params, loginUser.get)))
        resetCsrf
        render(s"/users/edit")
      }
    } else {
      haltWithBody(403)
    }
  }

}
