package controller.upload

import java.io._

import skinny.SkinnyConfig
import skinny.controller.AssetsController

class LocalUploadAssetsController extends AssetsController {
  protectFromForgery()

  addMimeMapping("image/png", "png")
  addMimeMapping("image/jpeg", "jpg")
  addMimeMapping("image/gif", "gif")

  def filename: Option[String] = multiParams("splat").headOption

  def file(): Any = {
    filename match {
      case Some(fn) =>
        SkinnyConfig.stringConfigValue("upload.local.baseDir") match {
          case Some(baseDir) =>
            val path = baseDir + '/' + fn
            val foundFile: Option[File] = path.map(p => new File(path)).find(_.exists)
            foundFile match {
              case Some(file) =>
                contentType = fn.split('.').lastOption.flatMap(formats.get).getOrElse("application/octet-stream")
                setLastModified(file.lastModified)
                if (isModified(file.lastModified)) {
                  new FileInputStream(file)
                } else halt(304)
              case _ => pass()
            }
          case _ => pass()
        }
      case _ => pass()
    }
  }

}
