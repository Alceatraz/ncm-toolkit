@file:Suppress("unused")

package top.btswork.ncmtoolkit.libengine.core

import top.btswork.ncmtoolkit.libengine.RuntimeBuilder
import top.btswork.ncmtoolkit.libengine.RuntimeView
import top.btswork.ncmtoolkit.libengine.TaskBinding
import top.btswork.ncmtoolkit.libengine.TaskBindingBuilder
import top.btswork.ncmtoolkit.libengine.TaskEngine
import top.btswork.ncmtoolkit.libengine.TaskEngineBuilder

interface TaskEngineFactory {
  fun getEngineBuilder(): TaskEngineBuilder
}

abstract class AbstractTaskEngineBuilder : TaskEngineBuilder {

  private var enable = true

  private val validator = Validator()

  override fun withInitial(key: String): TaskEngineBuilder {
    require(enable) { "TaskEngineBuilder is disabled" }
    validator.checkInitialKey(key) {
      withInitialImpl(key)
    }
    return this
  }

  override fun withContext(key: String, supplier: () -> Any) = withContext(key, supplier.invoke())

  override fun withContext(key: String, value: Any): TaskEngineBuilder {
    require(enable) { "TaskEngineBuilder is disabled" }
    validator.checkContextKey(key) {
      withContextImpl(key, value)
    }
    return this
  }

  override fun withBinding(binding: TaskBinding): TaskEngineBuilder {
    require(enable) { "TaskEngineBuilder is disabled" }
    validator.checkTask(binding) {
      withBindingImpl(binding)
    }
    return this
  }

  override fun withBinding(name: String, builder: TaskBindingBuilder. () -> Unit): TaskEngineBuilder =
    withBinding(TaskBindingBuilder(name).apply { builder() }.build())

  override fun build(): TaskEngine {
    require(enable) { "TaskEngineBuilder is disabled" }
    enable = false
    validator.checkBuild()
    return buildImpl()
  }

  private class Validator {

    private val existContext = mutableSetOf<String>()
    private val existInitial = mutableSetOf<String>()

    private val existTaskName = mutableSetOf<String>()
    private val existRequires = mutableSetOf<String>()
    private val existProduces = mutableSetOf<String>()

    fun checkInitialKey(name: String, func: () -> Unit) {
      require(existContext.contains(name).not()) { "Duplicate key name $name" }
      require(existInitial.contains(name).not()) { "Duplicate key name $name" }
      func()
      existInitial.add(name)
    }

    fun checkContextKey(name: String, func: () -> Unit) {
      require(existContext.contains(name).not()) { "Duplicate key name $name" }
      require(existInitial.contains(name).not()) { "Duplicate key name $name" }
      func()
      existContext.add(name)
    }

    fun checkTask(wrapper: TaskBinding, func: () -> Unit) {
      require(wrapper.requires.isNotEmpty()) { "TaskUnit must annotation more then one requires" }
      require(wrapper.produces.isNotEmpty()) { "TaskUnit must annotation more then one produces" }
      require(existTaskName.contains(wrapper.name).not()) { "Duplicate task name ${wrapper.name}" }
      wrapper.produces.forEach {
        require(existContext.contains(it).not()) { "Produce value $it already exists in context" }
        require(existInitial.contains(it).not()) { "Produce value $it already exists in initial" }
        require(existProduces.contains(it).not()) { "Produce value $it already exists in produce" }
      }
      func()
      existTaskName.add(wrapper.name)
      existRequires.addAll(wrapper.requires)
      existProduces.addAll(wrapper.produces)
    }

    fun checkBuild() {
      val exist = mutableSetOf<String>().apply {
        addAll(existContext)
        addAll(existInitial)
        addAll(existProduces)
      }
      val unexist = existRequires.filter { exist.contains(it).not() }
      require(unexist.isEmpty()) {
        "Required key not found in context/initial/produces: ${unexist.joinToString(", ")}"
      }
    }
  }

  protected abstract fun withInitialImpl(key: String)
  protected abstract fun withContextImpl(key: String, value: Any)
  protected abstract fun withBindingImpl(binding: TaskBinding)
  protected abstract fun buildImpl(): TaskEngine

}

abstract class AbstractTaskEngine : TaskEngine {

  private var enable = true

  override fun builder(): RuntimeBuilder {
    return builderImpl()
  }

  override fun execute(builder: RuntimeBuilder): RuntimeView {
    require(enable) { "TaskEngine running" }
    enable = false
    val runtime = (builder as AbstractRuntimeBuilder).internalBuild()
    executeImpl(runtime)
    enable = true
    return RuntimeViewImpl(runtime.toMap())
  }

  protected abstract fun builderImpl(): RuntimeBuilder
  protected abstract fun executeImpl(runtime: MutableMap<String, Any>)

}

abstract class AbstractRuntimeBuilder : RuntimeBuilder {

  private var enable = true

  override operator fun <T : Any> set(name: String, value: T): RuntimeBuilder {
    require(enable) { "Illegal state" }
    setImpl(name, value)
    return this
  }

  internal fun internalBuild(): MutableMap<String, Any> {
    require(enable) { "Illegal state" }
    enable = false
    return buildImpl()
  }

  protected abstract fun <T : Any> setImpl(key: String, value: T)
  protected abstract fun buildImpl(): MutableMap<String, Any>
}

@Suppress("UNCHECKED_CAST")
class RuntimeViewImpl(private val store: Map<String, Any>) : RuntimeView {
  override fun has(key: String) = store.containsKey(key)
  override fun <T> get(key: String, default: () -> T): T = store[key] as T ?: default()
  override fun <T> getOrNull(key: String): T? = store[key] as T?
  override fun <T> getOrThrow(key: String, message: () -> String): T = store[key] as T ?: error(message())
  override operator fun <T> get(key: String): T? = store[key] as T?
}