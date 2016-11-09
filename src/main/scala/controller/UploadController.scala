package controller

import lib.UploadedBaseURL
import skinny._
import skinny.controller.feature.FileUploadFeature
import skinny.routing.Routes
import skinny.validator._

abstract class UploadController extends SkinnyServlet with FileUploadFeature with ControllerBase with Routes {

  // --------------
  // policies
  val policiesUrl = post("/upload/policies")(policies).as('policies)

  // --------------
  // POST /upload/policies
  def policiesParams: Params = {
    loginUser.map { u =>
      Params(params ++ Map("user_id" -> u.userId))
    } getOrElse Params(params)
  }
  def policiesForm: MapValidator = validation(
    policiesParams,
    paramKey("user_id") is required,
    paramKey("filename") is required,
    paramKey("content_type") is required,
    paramKey("size") is required
  )
  def policies = {
    debugLoggingParameters(policiesForm)
    if (policiesForm.validate()) {
      sign
    } else {
      haltWithBody(403)
    }
  }

  protected def sign: Any

  // --------------
  // upload
  val uploadUrl = post("/upload/file")(upload).as('upload)

  // --------------
  // POST /upload/file
  protected def upload: Any

  // --------------
  def uploadedFileBaseUrl: UploadedBaseURL

}
