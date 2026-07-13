package top.btswork.ncmtoolkit.libtag.module.mp3

import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3ProcessorFactory
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Reader
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Writer
import top.btswork.ncmtoolkit.libtag.module.mp3.impl.Mp3ProcessorImpl

object LibMp3 {

  private val _ID3V2_MAGIC = byteArrayOf(0x49, 0x44, 0x33)

  private val _ID3V22_FLAG = byteArrayOf(0x02, 0x00, 0x00)
  private val _ID3V23_FLAG = byteArrayOf(0x03, 0x00, 0x00)
  private val _ID3V24_FLAG = byteArrayOf(0x04, 0x00, 0x00)

  val ID3V2_MAGIC get() = _ID3V2_MAGIC.clone()
  val ID3V22_FLAG get() = _ID3V22_FLAG.clone()
  val ID3V23_FLAG get() = _ID3V23_FLAG.clone()
  val ID3V24_FLAG get() = _ID3V24_FLAG.clone()

  private var factory: Mp3ProcessorFactory = Mp3ProcessorImpl()

  fun getFactory(): Mp3ProcessorFactory = this.factory
  fun setFactory(factory: Mp3ProcessorFactory) = also { this.factory = factory }

  fun getReader(): Mp3Reader = factory.getReader()
  fun getWriter(): Mp3Writer = factory.getWriter()

}