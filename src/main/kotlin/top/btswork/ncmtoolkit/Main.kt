package top.btswork.ncmtoolkit

import kotlinx.coroutines.runBlocking
import top.btswork.ncmtoolkit.app.Application
import top.btswork.ncmtoolkit.core.config.Configure
import top.btswork.ncmtoolkit.core.config.ConfigureDelegate

fun main(args: Array<String>) {

  val config = ConfigureDelegate.getInstance(args)

  val scope = ApplicationContext(config)
  val app = Application()

  with(app) {
    runBlocking {
      scope.start()
    }
  }

}

class ApplicationContext(
  val config: Configure,
)

