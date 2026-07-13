@file:Suppress("unused")

package top.btswork.ncmtoolkit.libengine

import top.btswork.ncmtoolkit.libengine.core.TaskEngineFactory
import top.btswork.ncmtoolkit.libengine.impl.TaskEngineFactoryImpl
import kotlin.reflect.KClass

object TaskEngines {

  private var factory: TaskEngineFactory = TaskEngineFactoryImpl()

  fun getFactory(): TaskEngineFactory = this.factory
  fun setFactory(factory: TaskEngineFactory) = also { this.factory = factory }

  fun getEngineBuilder() = factory.getEngineBuilder()

}

//= ============================================================================

interface TaskEngineBuilder {
  fun withInitial(key: String): TaskEngineBuilder
  fun withContext(key: String, value: Any): TaskEngineBuilder
  fun withContext(key: String, supplier: () -> Any): TaskEngineBuilder
  fun withBinding(binding: TaskBinding): TaskEngineBuilder
  fun withBinding(name: String, builder: TaskBindingBuilder. () -> Unit): TaskEngineBuilder
  fun build(): TaskEngine
}

interface TaskEngine {
  fun builder(): RuntimeBuilder
  fun execute(builder: RuntimeBuilder): RuntimeView
}

interface RuntimeBuilder {
  operator fun <T : Any> set(name: String, value: T): RuntimeBuilder
}

interface RuntimeView {
  fun has(key: String): Boolean
  fun <T> get(key: String, default: () -> T): T
  fun <T> getOrNull(key: String): T?
  fun <T> getOrThrow(key: String, message: () -> String = { "Key not exist: $key" }): T
  operator fun <T> get(key: String): T?
}

@DslMarker annotation class TaskScopeDsl

@TaskScopeDsl interface TaskScope {
  val scope: TaskScope
  operator fun <T : Any> get(key: String): T
  operator fun <T : Any> set(key: String, value: T)
  fun stop(reason: String? = null)
}

//= ============================================================================

interface TaskUnit {
  fun TaskScope.execute()
}

class TaskBinding(
  val name: String,
  val task: TaskUnit,
  val requires: Set<String>,
  val produces: Set<String>,
  val conditions: Set<TaskScope.() -> Boolean> = setOf { true },
)

class TaskBindingBuilder(val name: String) {
  var task: TaskUnit? = null
  var requires: (Set<String>)? = null
  var produces: (Set<String>)? = null
  var conditions: Set<TaskScope.() -> Boolean> = setOf { true }
  fun build() = TaskBinding(
    name,
    task ?: error("musicId missing"),
    requires ?: error("requires missing"),
    produces ?: error("produces missing"),
    conditions
  )
}

fun TaskBinding(name: String, func: TaskBindingBuilder. () -> Unit) =
  TaskBindingBuilder(name).apply { func() }.build()

//= ============================================================================

@Deprecated("Not implemented yet")

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Alias(
  val name: String,
)

@Deprecated("Not implemented yet") class TaskBindingWithReflect<T : Any, R : Any>(
  val name: String,
  val task: (T) -> R,
  val requires: KClass<T>,
  val produces: KClass<R>,
  val conditions: Set<TaskScope.() -> Boolean> = setOf { true },
)
