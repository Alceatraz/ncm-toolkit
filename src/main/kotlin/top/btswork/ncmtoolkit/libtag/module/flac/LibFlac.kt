package top.btswork.ncmtoolkit.libtag.module.flac

import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacProcessorFactory
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacReader
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacWriter
import top.btswork.ncmtoolkit.libtag.module.flac.impl.FlacProcessorFactoryImpl

object LibFlac {

  private val _FLAC_MAGIC = byteArrayOf(0x66, 0x4C, 0x61, 0x43) // "fLaC"

  val FLAC_MAGIC: ByteArray
    get() = _FLAC_MAGIC.clone()

  private var factory: FlacProcessorFactory = FlacProcessorFactoryImpl()

  fun getFactory(): FlacProcessorFactory = this.factory
  fun setFactory(factory: FlacProcessorFactory) = also { this.factory = factory }

  fun getReader(): FlacReader = factory.getReader()
  fun getWriter(): FlacWriter = factory.getWriter()

}

