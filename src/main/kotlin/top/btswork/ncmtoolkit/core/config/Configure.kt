package top.btswork.ncmtoolkit.core.config

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import kotlin.collections.iterator

object ConfigureDelegate {

  fun getInstance(arguments: Array<String>): Configure {
    val (args, options, parameters) = arguments.parse()
    val (path, mustExist) = if (parameters.containsKey("config")) {
      Paths.get(parameters.getValue("config")) to true
    } else {
      Paths.get("application.properties") to false
    }
    if (Files.exists(path)) {
      val properties = path.parse()
      for ((key, value) in properties) {
        if (parameters.containsKey(key)) continue
        parameters[key] = value
      }
    } else {
      if (mustExist) error { "Config file specific but not exist: $path" }
    }
    return Configure(args, options, parameters)
  }

  private fun Array<String>.parse(): Triple<MutableList<String>, MutableSet<String>, MutableMap<String, String>> {
    val args = mutableListOf<String>()
    val options = mutableSetOf<String>()
    val parameters = mutableMapOf<String, String>()
    var i = 0
    while (i < size) {
      val token = this[i]
      if (token.startsWith("--")) {
        val key = token.substring(2)
        if (i + 1 < size && !this[i + 1].startsWith("--")) {
          parameters[key] = this[i + 1]
          i += 2
        } else {
          options += key
          i++
        }
        continue
      }
      args += token
      i++
    }
    return Triple(args, options, parameters)
  }

  private fun Path.parse(): Map<String, String> = Properties().also { properties ->
    Files.newInputStream(this).use { properties.load(it) }
  }.map {
    val key = it.key.toString()
    require(it.value != null) {
      "Properties contains invalid key: $key"
    }
    val value = it.value.toString()
    key to value
  }.associate {
    it
  }
}

class Configure(
  private val args: List<String>,
  private val options: Set<String>,
  private val parameters: Map<String, String>,
) {
  fun getArgs() = args.toList()
  fun getOptions() = options.toSet()
  fun getParameters() = parameters.toMap()
  fun hasOption(name: String) = options.contains(name)
  fun hasParameter(name: String) = parameters.containsKey(name)
  fun getParameter(name: String, default: String) = parameters[name] ?: default
  fun getParameter(name: String, message: () -> String = { "Parameter not found $name" }) = parameters[name] ?: error(message())
}