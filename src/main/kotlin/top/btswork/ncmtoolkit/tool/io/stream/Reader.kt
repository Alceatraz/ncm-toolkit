package top.btswork.ncmtoolkit.tool.io.stream

import top.btswork.ncmtoolkit.tool.io.stream.impl.ByteBufferReader
import top.btswork.ncmtoolkit.tool.io.stream.impl.MappedReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

object Readers {
  fun getInstance(buffer: ByteBuffer) = ByteBufferReader(buffer)
  fun getInstance(channel: FileChannel) = MappedReader(channel)
}

@DslMarker annotation class ReaderDSL

@ReaderDSL interface Reader {

  fun getInteger8(): Byte
  fun getInteger16(): Short
  fun getInteger32(): Int
  fun getInteger64(): Long
  fun getDecimal32(): Float
  fun getDecimal64(): Double

  fun getInteger8(index: Int): Byte
  fun getInteger16(index: Int): Short
  fun getInteger32(index: Int): Int
  fun getInteger64(index: Int): Long
  fun getDecimal32(index: Int): Float
  fun getDecimal64(index: Int): Double

  fun seek(position: Int): ByteBuffer // C
  fun skip(distance: Int): ByteBuffer // C

  fun current(): Int // C
  fun remaining(): Int

  fun get(length: Int): ByteArray
  fun get(length: Int, index: Int = current()): ByteArray

  fun slice(length: Int, index: Int = current()): ByteBuffer

}

