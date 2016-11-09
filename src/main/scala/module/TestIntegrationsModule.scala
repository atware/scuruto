package module

import integration._

class TestIntegrationsModule extends scaldi.Module {

  bind[ExternalServiceIntegration] to LoggerIntegration

}
