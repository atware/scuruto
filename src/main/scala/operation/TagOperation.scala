package operation

import model.User._
import model._
import scalikejdbc.DBSession

/**
 * The operation for tags.
 */
sealed trait TagOperation extends OperationBase {

  def get(name: String)(implicit s: DBSession = autoSession): Option[Tag]

}

class TagOperationImpl extends TagOperation {

  override def get(name: String)(implicit s: DBSession = autoSession): Option[Tag] = {
    Tag.findByName(name)
  }

}