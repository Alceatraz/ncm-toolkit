package top.btswork.ncmtoolkit.libtag.module.flac.impl

import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacProcessorFactory

const val VERBOSE = false

class FlacProcessorFactoryImpl : FlacProcessorFactory {
  val readerImpl = FlacReaderImpl()
  val writerImpl = FlacWriterImpl()
  override fun getReader() = readerImpl
  override fun getWriter() = writerImpl
}
