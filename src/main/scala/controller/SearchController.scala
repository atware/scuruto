package controller

import operation.SearchOperation

class SearchController extends ApplicationController {
  protectFromForgery()

  // --------------
  // GET /search?q=xxx
  def search(q: String) = {
    val searchOperation = inject[SearchOperation]
    val pageNo: Int = params.getAs[Int]("page").getOrElse(1)
    val result = searchOperation.search(q, pageNo)
    set("result", result)
    render(s"/search/result")
  }

}
