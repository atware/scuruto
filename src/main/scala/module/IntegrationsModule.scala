package module

import integration._
import skinny.SkinnyConfig

class IntegrationsModule extends scaldi.Module {

  private val DEFAULT_SERVICE = "Null"

  val service = SkinnyConfig.stringConfigValue("externalIntegration.service").map { configValue =>
    configValue.capitalize
  } getOrElse DEFAULT_SERVICE

  import scala.reflect.runtime.universe
  val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
  val module = runtimeMirror.staticModule(s"integration.${service}Integration")
  val obj = runtimeMirror.reflectModule(module)
  val integration = obj.instance

  bind[ExternalServiceIntegration] to integration.asInstanceOf[ExternalServiceIntegration]
}