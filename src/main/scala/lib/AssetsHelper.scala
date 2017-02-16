package lib

import skinny.SkinnyEnv

import scala.io.Source

object AssetsHelper {
  val fileName = "version.txt"

  val hash = {
    if (SkinnyEnv.isDevelopment() || SkinnyEnv.isTest()) {
      val basePath = "src/main/webapp/assets/dist"
      Source.fromFile(s"${basePath}/${fileName}").mkString
    } else {
      val basePath = "assets/dist"
      Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(s"${basePath}/${fileName}")).mkString
    }
  }
}