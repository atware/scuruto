package controller

import skinny.SkinnyConfig

object UploadControllerFactory {

  private val DEFAULT_DESTINATION = "Local"

  val create: UploadController = {
    val destination = SkinnyConfig.stringConfigValue("upload.destination").map { configValue =>
      configValue.capitalize
    } getOrElse DEFAULT_DESTINATION

    import scala.reflect.runtime.universe
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(s"controller.upload.${destination}UploadController")
    val obj = runtimeMirror.reflectModule(module)
    val controller = obj.instance
    controller.asInstanceOf[UploadController]
  }

}
