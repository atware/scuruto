package lib

trait LoginProvidor {
  val name: String
  def isApp: Boolean = name.toLowerCase == "app"
}
object LoginProvidor {
  def apply(name: String): LoginProvidor = new LoginProvidorImpl(name)
}
private case class LoginProvidorImpl(name: String) extends LoginProvidor