package lib

import skinny.SkinnyConfig

trait UploadedBaseURL {
  val destination: String
  def value: String = {
    val configKey = s"upload.${destination.toLowerCase}.baseUrl"
    SkinnyConfig.stringConfigValue(configKey) match {
      case Some(url) => {
        if (url.last != '/') {
          url + "/"
        } else {
          url
        }
      }
      case None => throw new RuntimeException(s"config: '$configKey' is required.")
    }
  }
}
object UploadedBaseURL {
  def apply(destionation: String): UploadedBaseURL = new UploadedBaseURLImpl(destionation)
}
private case class UploadedBaseURLImpl(destination: String) extends UploadedBaseURL