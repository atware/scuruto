package operation

import skinny.PermittedStrongParameters
import skinny.logging.LoggerProvider

/**
 * The base service for this application.
 */
abstract class OperationBase extends LoggerProvider {

  def getParameterAsString(key: String, permittedAttributes: PermittedStrongParameters): String = {
    permittedAttributes.params.get(key).map(_._1).get.toString
  }

  def getParameterAsLong(key: String, permittedAttributes: PermittedStrongParameters): Long = {
    permittedAttributes.params.get(key).map(_._1).get.toString.toLong
  }

}
