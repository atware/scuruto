package controller

import model.typebinder.TagId
import model.{ Article, Tag }
import operation._

class TagsController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /tags/{name}
  def index(name: String) = {
    val tagOperation = inject[TagOperation]
    tagOperation.get(name).map { tag =>
      val articleOperation = inject[ArticleOperation]
      val pageNo: Int = params.getAs[Int]("page").getOrElse(1)
      val pagination = articleOperation.getPageByTag(tag, pageNo)
      set("tag", tag)
      set("items", pagination.data)
      set("pagination", pagination)
      render(s"/tags/list")
    } getOrElse haltWithBody(404)
  }

}
