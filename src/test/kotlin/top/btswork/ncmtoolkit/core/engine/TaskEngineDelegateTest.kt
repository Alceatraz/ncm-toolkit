package top.btswork.ncmtoolkit.core.engine

import top.btswork.ncmtoolkit.libengine.RuntimeBuilder
import top.btswork.ncmtoolkit.libengine.TaskBinding
import top.btswork.ncmtoolkit.libengine.TaskEngine
import top.btswork.ncmtoolkit.libengine.TaskEngines
import top.btswork.ncmtoolkit.libengine.TaskScope
import top.btswork.ncmtoolkit.libengine.TaskUnit
import kotlin.test.Test

const val NetworkEnableKey = "NETWORK_ENABLE"
const val InputKey = "input"
const val OutputKey = "output"

class TaskEngineDelegateTest {

  @Test
  fun test00() {

    val builder = TaskEngines.getEngineBuilder()

    builder.withContext(NetworkEnableKey, false)
    builder.withInitial(InputKey)

    val binding = TaskBinding("test") {

      requires = setOf(
        InputKey,
        NetworkEnableKey
      )

      produces = setOf(
        OutputKey
      )

      task = object : TaskUnit {

        override fun TaskScope.execute() {

          val temp: String = scope[InputKey]
          val bool: Boolean = scope[NetworkEnableKey]
          if (bool) {
            scope[OutputKey] = "ENABLED $temp"
          } else {
            scope[OutputKey] = "DISABLE $temp"
          }

          scope.stop()

        }

      }

    }

    builder.withBinding(binding)

    val engine: TaskEngine = builder.build()
    val runtimeBuilder: RuntimeBuilder = engine.builder()

    runtimeBuilder[InputKey] = "Hello, world!"

    val executeResult = engine.execute(runtimeBuilder)

    val output: String = executeResult.getOrThrow(OutputKey)

    println("output: $output")

  }

}