package top.btswork.ncmtoolkit.tool.io.stream

import java.nio.ByteBuffer

@DslMarker annotation class ReaderDSL

@ReaderDSL interface Reader {

  fun current(): Int
  fun remaining(): Int

  fun seek(position: Int)
  fun skip(distance: Int)

  fun get(length: Int): ByteArray

  fun getInteger8(): Byte
  fun getInteger16(): Short
  fun getInteger32(): Int
  fun getInteger64(): Long
  fun getDecimal32(): Float
  fun getDecimal64(): Double

  fun get(length: Int, index: Int): ByteArray

  fun getInteger8(index: Int): Byte
  fun getInteger16(index: Int): Short
  fun getInteger32(index: Int): Int
  fun getInteger64(index: Int): Long
  fun getDecimal32(index: Int): Float
  fun getDecimal64(index: Int): Double

  fun slice(length: Int, index: Int): ByteBuffer
  fun sub(length: Int, index: Int): Reader

}

