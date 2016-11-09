package controller

import model.typebinder.{ ArticleId, UserId }
import skinny.ParamType

object IdParamType {
  val UserId = ParamType {
    case v: String if v.trim.isEmpty => null
    case v: String => v.toLong
    case v: Long => v
    case v: Int => v.toLong
    case v: UserId => v.value
  }
  val ArticleId = ParamType {
    case v: String if v.trim.isEmpty => null
    case v: String => v.toLong
    case v: Long => v
    case v: Int => v.toLong
    case v: ArticleId => v.value
  }
}
