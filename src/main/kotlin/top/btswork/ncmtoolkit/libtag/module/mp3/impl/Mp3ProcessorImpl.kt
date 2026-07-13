package top.btswork.ncmtoolkit.libtag.module.mp3.impl

import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3ProcessorFactory

class Mp3ProcessorImpl : Mp3ProcessorFactory {
  private val reader = Mp3ReaderImpl()
  private val writer = Mp3WriterImpl()
  override fun getReader() = reader
  override fun getWriter() = writer
}

//= ============================================================================

fun Int.deSynchsafe(): Int {
  val b0 = (this ushr 24) and 0x7F
  val b1 = (this ushr 16) and 0x7F
  val b2 = (this ushr 8) and 0x7F
  val b3 = this and 0x7F
  return (b0 shl 21) or (b1 shl 14) or (b2 shl 7) or b3
}


