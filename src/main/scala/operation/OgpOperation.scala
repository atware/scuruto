package operation

import lib.{ Markdown, Util }
import model.Article
import model.typebinder.ArticleId
import scalikejdbc.DBSession
import skinny.SkinnyConfig
import skinny.micro.context.SkinnyContext
import view_model._

/**
 * The operation for OGP.
 */
sealed trait OgpOperation extends OperationBase {
  val ENVVAR_OGP_ALLOW_UA_PREFIX = "OGP_ALLOW_UA_PREFIX"

  def buildOgp(id: ArticleId)(implicit ctx: SkinnyContext, s: DBSession = Article.autoSession): Option[Ogp]

}

class OgpOperationImpl extends OgpOperation {

  private val allowUAPrefixes: Seq[String] = {
    SkinnyConfig.stringConfigValue("ogp.allowUAs").map { domains =>
      domains.split(",").toSeq
    } getOrElse Seq()
  }

  override def buildOgp(id: ArticleId)(implicit ctx: SkinnyContext, s: DBSession = Article.autoSession): Option[Ogp] = {
    val ua = ctx.request.getHeader("User-Agent")
    if (allowUAPrefixes.exists(prefix => ua != null && ua.startsWith(prefix))) {
      Article.findById(id).map { article =>
        Ogp(
          id,
          article.title,
          makeDescription(article.body),
          Util.baseURL(ctx.request) + "/assets/img/ogp.png",
          Util.baseURL(ctx.request) + "/articles/" + id
        )
      }
    } else {
      None
    }
  }

  private def makeDescription(body: String): String = {
    Markdown.toHtml(body).replaceAll("<(.*?)>", "").replaceAll("\n", "").take(50) + "..."
  }

}