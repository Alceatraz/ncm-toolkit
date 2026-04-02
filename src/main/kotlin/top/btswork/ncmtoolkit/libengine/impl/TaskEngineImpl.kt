package top.btswork.ncmtoolkit.libengine.impl

import top.btswork.ncmtoolkit.libengine.RuntimeBuilder
import top.btswork.ncmtoolkit.libengine.TaskBinding
import top.btswork.ncmtoolkit.libengine.TaskEngine
import top.btswork.ncmtoolkit.libengine.TaskEngineBuilder
import top.btswork.ncmtoolkit.libengine.TaskScope
import top.btswork.ncmtoolkit.libengine.TaskScopeDsl
import top.btswork.ncmtoolkit.libengine.core.AbstractRuntimeBuilder
import top.btswork.ncmtoolkit.libengine.core.AbstractTaskEngine
import top.btswork.ncmtoolkit.libengine.core.AbstractTaskEngineBuilder
import top.btswork.ncmtoolkit.libengine.core.TaskEngineFactory

internal class TaskEngineFactoryImpl : TaskEngineFactory {
  override fun getEngineBuilder(): TaskEngineBuilder = TaskEngineBuilderImpl()
}

private class TaskEngineBuilderImpl : AbstractTaskEngineBuilder() {

  private val initialStore = mutableSetOf<String>()
  private val contextStore = mutableMapOf<String, Any>()
  private val bindingStore = mutableListOf<TaskBinding>()

  override fun withInitialImpl(key: String) {
    initialStore.add(key)
  }

  override fun withContextImpl(key: String, value: Any) {
    contextStore[key] = value
  }

  override fun withBindingImpl(binding: TaskBinding) {
    bindingStore += binding
  }

  override fun buildImpl(): TaskEngine {

    val orchestrated = mutableListOf<List<TaskBinding>>()

    val available = mutableSetOf<String>().apply {
      initialStore.forEach { add(it) }
      contextStore.keys.forEach { add(it) }
    }
    val remaining = mutableListOf<TaskBinding>().apply {
      bindingStore.forEach {
        add(it)
      }
    }
    while (remaining.isNotEmpty()) {
      val ready = remaining.filter {
        it.requires.all(available::contains)
      }
      require(ready.isNotEmpty()) {
        "Build DAG failed: Cyclic dependency, Remain ${remaining.joinToString(", ") { it.name }}"
      }
      ready.forEach {
        available.addAll(it.produces)
      }
      orchestrated.add(ready)
      remaining.removeAll(ready)
    }
    return TaskEngineImpl(initialStore, contextStore, orchestrated)
  }
}

private class TaskEngineImpl(
  private val initial: Set<String>,
  private val context: Map<String, Any>,
  private val orchestrated: List<List<TaskBinding>>,
) : AbstractTaskEngine() {

  override fun builderImpl(): RuntimeBuilder {
    return RuntimeStatusBuilderImpl(initial)
  }

  override fun executeImpl(runtime: MutableMap<String, Any>) {

    val contextDelegate = ContextDelegate(context)
    val runtimeDelegate = RuntimeDelegate(runtime)

    for (batch in orchestrated) {

      for (binding in batch) {

        val satisfied = binding.requires.all {
          contextDelegate.has(it) || runtimeDelegate.has(it)
        }

        if (satisfied.not()) continue

        val scope = TaskScopeImpl(
          contextDelegate,
          runtimeDelegate,
          binding.requires,
          binding.produces,
        )

        if (binding.conditions.all { scope.it() }.not()) continue

        try {
          with(binding.task) { scope.execute() }
        } catch (e: Exception) {
          throw RuntimeException("Error executing task ${binding.name}", e)
        }

        if (scope.stop) {
          // TODO (" NOT READY")
        }

      }
    }
  }

}

private class RuntimeStatusBuilderImpl(private val initial: Set<String>) : AbstractRuntimeBuilder() {

  private val store = mutableMapOf<String, Any>()

  override fun <T : Any> setImpl(key: String, value: T) {
    require(initial.contains(key)) { "Illegal key: $key is not declared" }
    store[key] = value
  }

  override fun buildImpl(): MutableMap<String, Any> {
    val ready = initial.filter { store.containsKey(it).not() }
    require(ready.isEmpty()) {
      "Some required key missing: ${ready.joinToString(", ") { it }}"
    }
    return store.toMutableMap()
  }
}

@Suppress("UNCHECKED_CAST")
class ContextDelegate(private val store: Map<String, Any>) {
  fun has(key: String): Boolean = store.containsKey(key)
  fun <T : Any> get(key: String): T = store[key] as T
}

@Suppress("UNCHECKED_CAST")
class RuntimeDelegate(private val store: MutableMap<String, Any>) {
  fun has(key: String): Boolean = store.containsKey(key)
  fun <T : Any> get(key: String): T = store[key] as T
  fun <T : Any> set(key: String, value: T) = apply {
    require(store.containsKey(key).not()) { "Key already exists in value: $key" }
    store[key] = value
  }
}

@TaskScopeDsl class TaskScopeImpl internal constructor(
  private val contextActual: ContextDelegate,
  private val runtimeActual: RuntimeDelegate,
  private val requires: Set<String>,
  private val produces: Set<String>,
) : TaskScope {

  var stop = false
  var reason: String? = null

  override val scope get() = this

  override operator fun <T : Any> get(key: String): T {
    require(requires.contains(key)) {
      "Operation not permit: GET $key is not permit"
    }
    return when {
      contextActual.has(key) -> contextActual.get(key)
      runtimeActual.has(key) -> runtimeActual.get(key)
      else -> error("Not possible: Requires already checked")
    }
  }

  override operator fun <T : Any> set(key: String, value: T) {
    require(produces.contains(key)) {
      "Operation not permit: SET $key is not permit"
    }
    runtimeActual.set(key, value)
  }

  override fun stop(reason: String?) {
    this.stop = true
    this.reason = reason
  }

}
