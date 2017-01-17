package validator

import skinny.validator.ValidationRule

import scala.language.reflectiveCalls

object validChar extends ValidationRule {
  def name = "validChar"
  override def isValid(v: Any): Boolean = isEmpty(v) || !v.toString.exists(_.isSurrogate)
}

object validChars extends ValidationRule {
  def name = "validChar"
  override def isValid(s: Any): Boolean = {
    s match {
      case seq: Seq[Any] =>
        seq.forall { v =>
          isEmpty(v) || !v.toString.exists(_.isSurrogate)
        }
      case _ =>
        false
    }
  }
}

case class maxLengths(max: Int) extends ValidationRule {
  def name = "maxLength"
  override def messageParams = Seq(max.toString)
  def isValid(s: Any): Boolean = {
    s match {
      case seq: Seq[Any] =>
        seq.forall { v =>
          isEmpty(v) || {
            toHasSize(v).map(x => x.size <= max)
              .getOrElse(v.toString.length <= max)
          }
        }
      case _ =>
        false
    }
  }
}
