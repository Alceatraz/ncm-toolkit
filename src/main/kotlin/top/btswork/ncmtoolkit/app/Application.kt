package top.btswork.ncmtoolkit.app

import top.btswork.ncmtoolkit.ApplicationContext
import top.btswork.ncmtoolkit.app.mode.AppModeHelp

private const val HELP = """
toolkit json --cache '/Cache/NCM' '/Music/CloudMusic1' '/Music/CloudMusic2' '...'
toolkit cover --cache '/Cache/NCM' '/Music/CloudMusic1' '/Music/CloudMusic2' '...'
toolkit fetch --cache '/Cache/NCM' '/Music/CloudMusic1' '/Music/CloudMusic2' '...'
toolkit convert --cache '/Cache/NCM' --output '/Music/target' '/Music/CloudMusic1' '/Music/CloudMusic2' '...'
"""

class Application {

  fun ApplicationContext.start() {

    if (config.getArgs().isEmpty()) {
      throw IllegalArgumentException("[FATAL] USAGE incorrect $HELP")
    }

    val app = when (config.getArgs()[0]) {
      "help" -> AppModeHelp
      "json" -> TODO() // AppModeJson
      "cover" -> {
        TODO()
      }
      "fetch" -> {
        TODO()
      }
      "convert" -> {
        TODO()
      }
      else -> throw IllegalArgumentException("[FATAL] USAGE incorrect $HELP")
    }

    app.execute(this)

  }

}

interface AppMode {
  fun execute(context: ApplicationContext)
}