package top.btswork.ncmtoolkit.tool.io.stream.core

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import java.nio.ByteBuffer

class ByteBufferReader(
  private val delegate: ByteBuffer,
) : Reader {

  override fun current(): Int = delegate.position()
  override fun remaining(): Int = delegate.remaining()

  override fun seek(position: Int) {
    delegate.position(position)
  }

  override fun skip(distance: Int) {
    delegate.position(current() + distance)
  }

  override fun get(length: Int): ByteArray {
    require(length <= delegate.remaining()) {
      "ByteBuffer remain content less then $length byte"
    }
    return ByteArray(length).also { delegate.get(it) }
  }

  override fun getInteger8(): Byte {
    require(1 <= delegate.remaining()) {
      "ByteBuffer remain content less then 1 byte"
    }
    return delegate.get()
  }

  override fun getInteger16(): Short {
    require(2 <= delegate.remaining()) {
      "ByteBuffer remain content less then 2 bytes"
    }
    return delegate.getShort()
  }

  override fun getInteger32(): Int {
    require(4 <= delegate.remaining()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    return delegate.getInt()
  }

  override fun getInteger64(): Long {
    require(8 <= delegate.remaining()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    return delegate.getLong()
  }

  override fun getDecimal32(): Float {
    require(4 <= delegate.remaining()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    return delegate.getFloat()
  }

  override fun getDecimal64(): Double {
    require(8 <= delegate.remaining()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    return delegate.getDouble()
  }

  override fun get(length: Int, index: Int): ByteArray {
    require(index + length <= delegate.limit()) {
      "ByteBuffer remain content less then $length byte"
    }
    return ByteArray(length).also { delegate.get(it) }
  }

  override fun getInteger8(index: Int): Byte {
    require(index + 1 <= delegate.limit()) {
      "ByteBuffer remain content less then 1 byte"
    }
    return delegate.get(index)
  }

  override fun getInteger16(index: Int): Short {
    require(index + 2 <= delegate.limit()) {
      "ByteBuffer remain content less then 2 bytes"
    }
    return delegate.getShort(index)
  }

  override fun getInteger32(index: Int): Int {
    require(index + 4 <= delegate.limit()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    return delegate.getInt(index)
  }

  override fun getInteger64(index: Int): Long {
    require(index + 8 <= delegate.limit()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    return delegate.getLong(index)
  }

  override fun getDecimal32(index: Int): Float {
    require(index + 4 <= delegate.limit()) {
      "ByteBuffer remain content less then 4 bytes"
    }
    return delegate.getFloat(index)
  }

  override fun getDecimal64(index: Int): Double {
    require(index + 8 <= delegate.limit()) {
      "ByteBuffer remain content less then 8 bytes"
    }
    return delegate.getDouble(index)
  }

  override fun slice(length: Int, index: Int): ByteBuffer {
    return delegate.slice(index, length)
  }

  override fun sub(length: Int, index: Int): Reader {
    return ByteBufferReader(delegate.slice(index, length))
  }
}