package lib

trait UploadDestination {
  val name: String
  def isLocal: Boolean = name.toLowerCase == "local"
}
object UploadDestination {
  def apply(name: String): UploadDestination = new UploadDestinationImpl(name)
}
private case class UploadDestinationImpl(name: String) extends UploadDestination