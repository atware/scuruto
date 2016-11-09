package controller

import skinny.SkinnyConfig

object LoginControllerFactory {

  private val DEFAULT_PROVIDOR = "App"

  val create: LoginController = {
    val providor = SkinnyConfig.stringConfigValue("login.providor").map { configValue =>
      configValue.capitalize
    } getOrElse DEFAULT_PROVIDOR

    import scala.reflect.runtime.universe
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(s"controller.login.${providor}LoginController")
    val obj = runtimeMirror.reflectModule(module)
    val controller = obj.instance
    controller.asInstanceOf[LoginController]
  }

}
