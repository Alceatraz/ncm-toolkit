package top.btswork.ncmtoolkit.tool.io.stream

@DslMarker annotation class ContextIODSL

@ContextIODSL class ContextIO(
  private val reader: Reader,
  private val writer: Writer,
) {

  fun readerCurrent(): Int = reader.current()
  fun readerRemaining(): Int = reader.remaining()
  fun readerSeek(position: Int) = reader.seek(position)
  fun readerSkip(distance: Int) = reader.skip(distance)
  fun readerSlice(length: Int, index: Int = readerCurrent()) = reader.slice(length, index)
  fun readerSub(length: Int, index: Int = readerCurrent()) = reader.sub(length, index)

  fun get(length: Int, index: Int = readerCurrent()) = reader.get(length, index)
  fun getInteger8(index: Int = readerCurrent()): Byte = reader.getInteger8(index)
  fun getInteger16(index: Int = readerCurrent()): Short = reader.getInteger16(index)
  fun getInteger32(index: Int = readerCurrent()): Int = reader.getInteger32(index)
  fun getInteger64(index: Int = readerCurrent()): Long = reader.getInteger64(index)
  fun getDecimal32(index: Int = readerCurrent()): Float = reader.getDecimal32(index)
  fun getDecimal64(index: Int = readerCurrent()): Double = reader.getDecimal64(index)

  fun writerCurrent(): Int = writer.current()
  fun writerCapacity(): Int = writer.capacity()
  fun writerSeek(position: Int) = writer.seek(position)
  fun writerSkip(distance: Int) = writer.skip(distance)

  fun set(length: Int, index: Int = writerCurrent(), value: ByteArray) = writer.set(length, index, value)
  fun setInteger8(index: Int = writerCurrent(), value: Byte) = writer.setInteger8(index, value)
  fun setInteger16(index: Int = writerCurrent(), value: Short) = writer.setInteger16(index, value)
  fun setInteger32(index: Int = writerCurrent(), value: Int) = writer.setInteger32(index, value)
  fun setInteger64(index: Int = writerCurrent(), value: Long) = writer.setInteger64(index, value)
  fun setDecimal32(index: Int = writerCurrent(), value: Float) = writer.setDecimal32(index, value)
  fun setDecimal64(index: Int = writerCurrent(), value: Double) = writer.setDecimal64(index, value)

}