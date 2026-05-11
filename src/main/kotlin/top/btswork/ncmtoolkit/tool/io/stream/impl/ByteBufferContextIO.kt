package top.btswork.ncmtoolkit.tool.io.stream.impl

import top.btswork.ncmtoolkit.tool.io.stream.ContextIO
import java.nio.ByteBuffer

class ByteBufferContextIO(
  readerBuffer: ByteBuffer,
  writerBuffer: ByteBuffer,
) : ContextIO {

  private val reader = ByteBufferReader(readerBuffer)
  private val writer = ByteBufferWriter(writerBuffer)

  override fun getReader() = reader
  override fun getWriter() = writer

  override fun copy(length: Int, srcIndex: Int, dstIndex: Int) {
    TODO("Not yet implemented")
  }

}