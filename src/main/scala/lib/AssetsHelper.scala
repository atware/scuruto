package lib

import scala.io.Source

object AssetsHelper {
  val basePath = "src/main/webapp/assets/dist"
  val fileName = "version.txt"

  val hash = Source.fromFile(s"${basePath}/${fileName}").mkString
}
