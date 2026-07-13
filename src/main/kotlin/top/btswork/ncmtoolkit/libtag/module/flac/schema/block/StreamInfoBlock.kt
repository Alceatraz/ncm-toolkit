package top.btswork.ncmtoolkit.libtag.module.flac.schema.block

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer

object StreamInfoBlocks {

  fun getInstance(
    minBlockSize: Int,
    maxBlockSize: Int,
    minFrameSize: Int,
    maxFrameSize: Int,
    sampleRate: Int,
    channels: Int,
    bitsPerSample: Int,
    totalSamples: Long,
    md5: ByteArray,
  ) = StreamInfoBlock(
    minBlockSize, maxBlockSize, minFrameSize, maxFrameSize,
    sampleRate, channels, bitsPerSample, totalSamples, md5,
  )

  fun Reader.parse(): StreamInfoBlock {
    TODO()
  }

  fun Writer.write(block: StreamInfoBlock) {

  }

}

data class StreamInfoBlock(
  val minBlockSize: Int,     // u16
  val maxBlockSize: Int,     // u16
  val minFrameSize: Int,     // u24
  val maxFrameSize: Int,     // u24
  val sampleRate: Int,       // u20
  val channels: Int,         // 1..8 (stored u3, +1)
  val bitsPerSample: Int,    // 4..32 (stored u5, +1)
  val totalSamples: Long,    // u36
  val md5: ByteArray,        // 128-bit
) : Block {

  override fun getType(): Byte = STREAMINFO

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StreamInfoBlock

    if (minBlockSize != other.minBlockSize) return false
    if (maxBlockSize != other.maxBlockSize) return false
    if (minFrameSize != other.minFrameSize) return false
    if (maxFrameSize != other.maxFrameSize) return false
    if (sampleRate != other.sampleRate) return false
    if (channels != other.channels) return false
    if (bitsPerSample != other.bitsPerSample) return false
    if (totalSamples != other.totalSamples) return false
    if (!md5.contentEquals(other.md5)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = minBlockSize
    result = 31 * result + maxBlockSize
    result = 31 * result + minFrameSize
    result = 31 * result + maxFrameSize
    result = 31 * result + sampleRate
    result = 31 * result + channels
    result = 31 * result + bitsPerSample
    result = 31 * result + totalSamples.hashCode()
    result = 31 * result + md5.contentHashCode()
    return result
  }
}