package controller

import model._
import org.scalatest._
import skinny.SkinnyConfig
import skinny.test.scalatest.SkinnyFlatSpec
import skinny.test.{ FactoryGirl, SkinnyTestSupport }

class OGPSpec extends SkinnyFlatSpec with Matchers with SkinnyTestSupport with TestDBSettings {
  addFilter(Controllers.articles, "/*")

  it should "redirect to / with ref param when not logged in" in {
    get(uri = "/articles/1?dummy=a", headers = Map("User-Agent" -> "Browser")) {
      status should equal(302)
      header.get("Location").get should endWith("/?ref=%2Farticles%2F1%3Fdummy%3Da") // with query string
    }
  }

  it should "redirect to / with ref param when OGP request with not logged in" in {
    get(uri = "/articles/1?o", headers = Map("User-Agent" -> "Browser")) {
      status should equal(302)
      header.get("Location").get should endWith("/?ref=%2Farticles%2F1") // without o parameter
    }
  }

  it should "redirect to / with ref param when OGP request with valid UA" in {
    FactoryGirl(User).withVariables('userId -> 1).create()
    FactoryGirl(Article).withVariables('articleId -> 1).create()

    val validUA = SkinnyConfig.stringConfigValue("ogp.allowUAs").get + "V2"
    get(uri = "/articles/1?o", headers = Map("User-Agent" -> validUA)) {
      status should equal(200)
      body should include("og:")
    }
  }

}
