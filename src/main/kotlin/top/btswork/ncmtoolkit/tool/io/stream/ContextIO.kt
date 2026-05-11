package top.btswork.ncmtoolkit.tool.io.stream

@DslMarker annotation class ContextIODSL

@ContextIODSL interface ContextIO {

  fun getReader(): Reader
  fun getWriter(): Writer

  fun copy(length: Int, srcIndex: Int = getReader().current(), dstIndex: Int = getWriter().current())

}