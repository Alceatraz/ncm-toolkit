package top.btswork.ncmtoolkit.tool.io.stream.core

import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

class ByteBufferWriter(
  private val delegate: ByteBuffer,
) : Writer {

  override fun current(): Int = delegate.position()
  override fun capacity(): Int = delegate.capacity()

  override fun seek(position: Int) {
    delegate.position(position)
  }

  override fun skip(distance: Int) {
    delegate.position(current() + distance)
  }

  override fun set(length: Int, value: ByteArray) {
    TODO("Not yet implemented")
  }

  override fun setInteger8(value: Byte) {
    TODO("Not yet implemented")
  }

  override fun setInteger16(value: Short) {
    TODO("Not yet implemented")
  }

  override fun setInteger32(value: Int) {
    TODO("Not yet implemented")
  }

  override fun setInteger64(value: Long) {
    TODO("Not yet implemented")
  }

  override fun setDecimal32(value: Float) {
    TODO("Not yet implemented")
  }

  override fun setDecimal64(value: Double) {
    TODO("Not yet implemented")
  }

  override fun set(length: Int, index: Int, value: ByteArray) {
    TODO("Not yet implemented")
  }

  override fun setInteger8(index: Int, value: Byte) {
    require(index + 1 <= delegate.capacity()) {
      "ByteBuffer capacity less then 1 byte"
    }
    delegate.put(index, value)
  }

  override fun setInteger16(index: Int, value: Short) {
    require(index + 2 <= delegate.capacity()) {
      "ByteBuffer capacity less then 2 byte"
    }
    delegate.putShort(index, value)
  }

  override fun setInteger32(index: Int, value: Int) {
    require(index + 4 <= delegate.capacity()) {
      "ByteBuffer capacity less then 4 byte"
    }
    delegate.putInt(index, value)
  }

  override fun setInteger64(index: Int, value: Long) {
    require(index + 8 <= delegate.capacity()) {
      "ByteBuffer capacity less then 8 byte"
    }
    delegate.putLong(index, value)
  }

  override fun setDecimal32(index: Int, value: Float) {
    require(index + 4 <= delegate.capacity()) {
      "ByteBuffer capacity less then 4 byte"
    }
    delegate.putFloat(index, value)
  }

  override fun setDecimal64(index: Int, value: Double) {
    require(index + 8 <= delegate.capacity()) {
      "ByteBuffer capacity less then 8 byte"
    }
    delegate.putDouble(index, value)
  }
}