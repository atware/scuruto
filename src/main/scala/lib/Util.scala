package lib

import javax.servlet.http.HttpServletRequest

object Util {
  def baseURL(request: HttpServletRequest): String = {
    val forwardedProto: String = request.getHeader("X-Forwarded-Proto")
    val scheme: String = if (forwardedProto == null) request.getScheme else forwardedProto
    val domain: String = request.getServerName
    val forwardedPort: String = request.getHeader("X-Forwarded-Port")
    val port: Int = if (forwardedPort == null) request.getServerPort else forwardedPort.toInt
    val _port = {
      if (port == 80 || port == 443) {
        ""
      } else {
        ":" + port.toString
      }
    }
    scheme + "://" + domain + _port
  }

}
