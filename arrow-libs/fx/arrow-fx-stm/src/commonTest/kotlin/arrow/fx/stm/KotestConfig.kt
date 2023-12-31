package arrow.fx.stm

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.property.PropertyTesting

class KotestConfig : AbstractProjectConfig() {
  override suspend fun beforeProject() {
    PropertyTesting.defaultIterationCount = 250
  }
}
