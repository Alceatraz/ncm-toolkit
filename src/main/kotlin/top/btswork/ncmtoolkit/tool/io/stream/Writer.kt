package top.btswork.ncmtoolkit.tool.io.stream

import top.btswork.ncmtoolkit.tool.io.stream.impl.ByteBufferWriter
import top.btswork.ncmtoolkit.tool.io.stream.impl.MappedWriter
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

object Writers {
  fun getInstance(buffer: ByteBuffer) = ByteBufferWriter(buffer)
  fun getInstance(channel: FileChannel) = MappedWriter(channel)
}

@DslMarker annotation class WriterDSL

@WriterDSL interface Writer {

  fun setInteger8(value: Byte)
  fun setInteger16(value: Short)
  fun setInteger32(value: Int)
  fun setInteger64(value: Long)
  fun setDecimal32(value: Float)
  fun setDecimal64(value: Double)

  fun setInteger8(value: () -> Byte)
  fun setInteger16(value: () -> Short)
  fun setInteger32(value: () -> Int)
  fun setInteger64(value: () -> Long)
  fun setDecimal32(value: () -> Float)
  fun setDecimal64(value: () -> Double)

  fun setInteger8(index: Int, value: Byte)
  fun setInteger16(index: Int, value: Short)
  fun setInteger32(index: Int, value: Int)
  fun setInteger64(index: Int, value: Long)
  fun setDecimal32(index: Int, value: Float)
  fun setDecimal64(index: Int, value: Double)

  fun setInteger8(index: Int, value: () -> Byte)
  fun setInteger16(index: Int, value: () -> Short)
  fun setInteger32(index: Int, value: () -> Int)
  fun setInteger64(index: Int, value: () -> Long)
  fun setDecimal32(index: Int, value: () -> Float)
  fun setDecimal64(index: Int, value: () -> Double)

  fun seek(position: Int): ByteBuffer // C
  fun skip(distance: Int): ByteBuffer // C

  fun current(): Int // C
  fun capacity(): Int

  fun set(value: ByteArray)
  fun set(value: () -> ByteArray)
  fun set(index: Int = current(), value: ByteArray)
  fun set(index: Int = current(), value: () -> ByteArray)
  fun set(index: Int = current(), limit: Int, value: ByteArray)
  fun set(index: Int = current(), limit: Int, value: () -> ByteArray)

  fun write(value: ByteBuffer)
  fun write(value: () -> ByteBuffer)
  fun write(index: Int = current(), value: ByteBuffer)
  fun write(index: Int = current(), value: () -> ByteBuffer)
  fun write(index: Int = current(), limit: Int, value: ByteBuffer)
  fun write(index: Int = current(), limit: Int, value: () -> ByteBuffer)

}