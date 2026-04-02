package top.btswork.ncmtoolkit.tool.io.stream

@DslMarker annotation class WriterDSL

@WriterDSL interface Writer {

  fun current(): Int
  fun capacity(): Int

  fun seek(position: Int)
  fun skip(distance: Int)

  fun set(length: Int, value: ByteArray)

  fun setInteger8(value: Byte)
  fun setInteger16(value: Short)
  fun setInteger32(value: Int)
  fun setInteger64(value: Long)
  fun setDecimal32(value: Float)
  fun setDecimal64(value: Double)

  fun set(length: Int, index: Int = current(), value: ByteArray)

  fun setInteger8(index: Int = current(), value: Byte)
  fun setInteger16(index: Int = current(), value: Short)
  fun setInteger32(index: Int = current(), value: Int)
  fun setInteger64(index: Int = current(), value: Long)
  fun setDecimal32(index: Int = current(), value: Float)
  fun setDecimal64(index: Int = current(), value: Double)

}