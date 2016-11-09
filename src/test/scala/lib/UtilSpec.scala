package lib

import org.scalatest.{ FunSpec, Matchers }
import skinny.test.MockHttpServletRequest

class UtilSpec extends FunSpec with Matchers {

  describe("baseURL") {

    it("builds url by http request") {
      val req = createRequest("http", "example.com", 80)
      Util.baseURL(req) should equal("http://example.com")
    }

    it("builds url by https request") {
      val req = createRequest("https", "example.com", 443)
      Util.baseURL(req) should equal("https://example.com")
    }

    it("builds url by localhost with 8080 port") {
      val req = createRequest("http", "localhost", 8080)
      Util.baseURL(req) should equal("http://localhost:8080")
    }

    it("builds url by request header") {
      val req = createRequest("http", "example.com", 8080)
      req.doAddHeaderValue("X-Forwarded-Proto", "https", replace = true)
      req.doAddHeaderValue("X-Forwarded-Port", "443", replace = true)

      Util.baseURL(req) should equal("https://example.com")
    }

    it("builds url by request header with non-standard port") {
      val req = createRequest("http", "example.com", 8080)
      req.doAddHeaderValue("X-Forwarded-Proto", "https", replace = true)
      req.doAddHeaderValue("X-Forwarded-Port", "8443", replace = true)

      Util.baseURL(req) should equal("https://example.com:8443")
    }

  }

  private def createRequest(scheme: String, serverName: String, serverPort: Int): MockHttpServletRequest = {
    val req = new MockHttpServletRequest
    req.scheme = scheme
    req.serverName = serverName
    req.serverPort = serverPort
    req
  }

}
