package controller

import lib.Markdown
import model.Tag

/**
 * The controller for API endpoints.
 */
class ApiController extends ApplicationController {

  // --------------
  // POST /api/preview
  def preview = {
    val body = params("body")
    val markdown = Markdown(body)
    val html = {
      try {
        markdown.html
      } catch {
        case e: Throwable =>
          logger.error(s"Failed to convert markdown to html; body=$body", e)
          "<p>Failed to convert markdown to html</p>"
      }
    }
    toJSONString(Map("html" -> html))
  }

  // --------------
  // GET /api/tags
  def tags = {
    val q = params.getOrElse('q, "")
    if (q != "") {
      toJSONString(Tag.findByNamePrefix(q).map { _.name })
    }
  }

}

