package controller

import java.util.Locale

import lib.{ SessionAttribute, Util }
import model.User
import skinny.PermittedStrongParameters
import skinny.controller.feature.{ FlashFeature, LocaleFeature, RequestScopeFeature }
import skinny.filter._
import skinny.micro.SkinnyMicroBase
import skinny.micro.context.SkinnyContext
import skinny.micro.contrib.CSRFTokenSupport
import skinny.validator.MapValidator

/**
 * The base feature for controllers.
 */
trait ControllerBase
    extends SkinnyMicroBase with RequestScopeFeature with FlashFeature with CSRFTokenSupport with LocaleFeature with SkinnySessionFilter
    with TxPerRequestFilter {

  override def defaultLocale = Some(Locale.JAPANESE)

  def loginUser(implicit ctx: SkinnyContext): Option[User] = skinnySession.getAttribute(SessionAttribute.LoginUser.key).asInstanceOf[Option[User]]

  /*
   * util
   */

  protected def baseURL(implicit ctx: SkinnyContext): String = {
    Util.baseURL(request)
  }

  protected def resetCsrf(implicit ctx: SkinnyContext): Unit = {
    if (getFromRequestScope(RequestScopeFeature.ATTR_CSRF_KEY).isEmpty) {
      set(RequestScopeFeature.ATTR_CSRF_KEY, csrfKey)
      set(RequestScopeFeature.ATTR_CSRF_TOKEN, prepareCsrfToken())
    }
  }

  protected def getRequiredParam[T](name: String)(implicit ctx: SkinnyContext): T = {
    params.get(name) match {
      case Some(value) => value.asInstanceOf[T]
      case _ => throw new IllegalStateException(s"cannot get from params. param-name: '$name'")
    }
  }

  /*
   * debug logging
   */

  protected def debugLoggingParameters(form: MapValidator, id: Option[Long] = None) = {
    if (logger.isDebugEnabled) {
      val forId = id.map { id => s" for [id -> ${id}]" }.getOrElse("")
      val params = form.paramMap.map { case (name, value) => s"${name} -> '${value}'" }.mkString("[", ", ", "]")
      logger.debug(s"Parameters${forId}: ${params}")
    }
  }

  protected def debugLoggingPermittedParameters(parameters: PermittedStrongParameters, id: Option[Long] = None) = {
    if (logger.isDebugEnabled) {
      val forId = id.map { id => s" for [id -> ${id}]" }.getOrElse("")
      val params = parameters.params.map { case (name, (v, t)) => s"${name} -> '${v}' as ${t}" }.mkString("[", ", ", "]")
      logger.debug(s"Permitted parameters${forId}: ${params}")
    }
  }

}
