package controller

import org.scalatest.Matchers
import skinny.session.SkinnyHttpSession
import skinny.test.MockController

trait ControllerSpec extends Matchers {

  def renderPath(implicit controller: MockController): String = controller.renderCall.map(_.path).getOrElse(fail())

  def initSession(implicit controller: MockController): Unit = SkinnyHttpSession.getOrCreate(controller.request)

  def sessionValue[A](key: String)(implicit controller: MockController): Option[A] = {
    SkinnyHttpSession.getOrCreate(controller.request).getAs[A](key)
  }

  def okHtmlResponse(implicit controller: MockController): Unit = {
    controller.status should equal(200)
    controller.contentType should equal("text/html; charset=utf-8")
  }

  def prepareSessionValue(key: String, value: Any)(implicit controller: MockController): Unit = {
    SkinnyHttpSession.getOrCreate(controller.request).setAttribute(key, value)
  }

}
