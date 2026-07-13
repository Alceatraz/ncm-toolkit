package top.btswork.ncmtoolkit.tool.io.stream.impl

import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

class ByteBufferWriter(
  val delegate: ByteBuffer,
) : Writer {

  override fun setInteger8(value: () -> Byte) = setInteger8(value())
  override fun setInteger16(value: () -> Short) = setInteger16(value())
  override fun setInteger32(value: () -> Int) = setInteger32(value())
  override fun setInteger64(value: () -> Long) = setInteger64(value())
  override fun setDecimal32(value: () -> Float) = setDecimal32(value())
  override fun setDecimal64(value: () -> Double) = setDecimal64(value())

  override fun setInteger8(value: Byte) {
    require(1 <= delegate.remaining()) {
      "ByteBuffer remain content less then 1 byte"
    }
    delegate.put(value)
  }

  override fun setInteger16(value: Short) {
    require(2 <= delegate.remaining()) {
      "ByteBuffer remain content less then 2 bytes"
    }
    delegate.putShort(value)
  }

  override fun setInteger32(value: Int) {
    require(4 <= delegate.remaining()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    delegate.putInt(value)
  }

  override fun setInteger64(value: Long) {
    require(8 <= delegate.remaining()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    delegate.putLong(value)
  }

  override fun setDecimal32(value: Float) {
    require(4 <= delegate.remaining()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    delegate.putFloat(value)
  }

  override fun setDecimal64(value: Double) {
    require(8 <= delegate.remaining()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    delegate.putDouble(value)
  }

  override fun setInteger8(index: Int, value: () -> Byte) = setInteger8(index, value())
  override fun setInteger16(index: Int, value: () -> Short) = setInteger16(index, value())
  override fun setInteger32(index: Int, value: () -> Int) = setInteger32(index, value())
  override fun setInteger64(index: Int, value: () -> Long) = setInteger64(index, value())
  override fun setDecimal32(index: Int, value: () -> Float) = setDecimal32(index, value())
  override fun setDecimal64(index: Int, value: () -> Double) = setDecimal64(index, value())

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

  override fun seek(position: Int): ByteBuffer = delegate.position(position)
  override fun skip(distance: Int): ByteBuffer = delegate.position(current() + distance)

  override fun current(): Int = delegate.position()
  override fun capacity(): Int = delegate.capacity()

  override fun set(value: () -> ByteArray) = set(value())

  override fun set(value: ByteArray) {
    val length = value.size
    require(length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    delegate.put(value)
  }

  override fun set(index: Int, value: () -> ByteArray) = set(index, value())

  override fun set(index: Int, value: ByteArray) {
    val length = value.size
    require(index + length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    val current = current()
    delegate.position(index)
    delegate.put(value)
    delegate.position(current)
  }

  override fun set(index: Int, limit: Int, value: () -> ByteArray) = set(index, limit, value())

  override fun set(index: Int, limit: Int, value: ByteArray) {
    val length = value.size
    require(index + length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    val current = current()
    delegate.position(index)
    delegate.put(value)
    delegate.position(current)
  }

  override fun write(value: () -> ByteBuffer) = write(value())
  override fun write(index: Int, value: () -> ByteBuffer) = write(index, value())
  override fun write(index: Int, limit: Int, value: () -> ByteBuffer) = write(index, limit, value())

  override fun write(value: ByteBuffer) {
    val length = value.limit()
    require(length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    delegate.put(value)
  }

  override fun write(index: Int, value: ByteBuffer) {
    val length = value.limit()
    require(index + length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    val current = current()
    delegate.position(index)
    delegate.put(value)
    delegate.position(current)
  }

  override fun write(index: Int, limit: Int, value: ByteBuffer) {
    val length = value.limit()
    require(index + length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    val current = current()
    delegate.position(index)
    delegate.put(value)
    delegate.position(current)
  }
}