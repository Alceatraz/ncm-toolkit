package top.btswork.ncmtoolkit.libtag.module.flac.impl

import top.btswork.ncmtoolkit.libtag.module.flac.LibFlac
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacReader
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacStream
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.BasicBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Block
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Colors
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Depth
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Description
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Height
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.MimeType
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.PaddingBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Picture
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.PictureBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.PictureType
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.StreamInfoBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.VorbisCommentBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.VorbisCommentBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Width
import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.reverseBytes
import java.nio.charset.StandardCharsets

const val enableVendorTrim = false
const val enableThrowWhenFalseSync = true

class FlacReaderImpl : FlacReader {

  override fun Reader.checkMagic(): Boolean = LibFlac.FLAC_MAGIC contentEquals get(4)

  //= ==================================================================================================================

  override fun Reader.parseBlocks(): FlacBlocks {

    val blocks = mutableListOf<Block>()

    while (true) {

      val start = current()

      val head = getInteger32()

      val lastFlag = head < 0
      val blockType = head shr 24 and 0b01111111
      val blockLength: Int = head and 0b00000000_11111111_11111111_11111111

      if (DEBUG) println("BLOCK $start -> $lastFlag: $blockType size=$blockLength")

      val block = when (blockType) {
        0 -> parseStreamInfoBlock()
        1 -> parsePaddingBlock(blockLength)
        6 -> parsePictureBlock()
        4 -> parseVorbisCommentBlock()
        else -> {
          val blockPayload = slice(blockLength)
          skip(blockLength)
          BasicBlocks.getInstance(blockType.toByte(), blockPayload)
        }
      }

      blocks.add(block)

      if (lastFlag) break

    }

    return FlacBlocks(blocks)

  }

  private fun Reader.parseStreamInfoBlock(): StreamInfoBlock {

    val minBlockSize = getInteger16().toInt() and 0xFFFF
    val maxBlockSize = getInteger16().toInt() and 0xFFFF
    val minFrameSize = readU24()
    val maxFrameSize = readU24()

    // 64-bit packed: sampleRate(20) + channels(3) + bitsPerSample(5) + totalSamples(36)
    val packed = getInteger64()

    val sampleRate = ((packed ushr 44) and 0xFFFFFL).toInt()
    val channels = ((packed ushr 41) and 0x7L).toInt() + 1
    val bitsPerSample = ((packed ushr 36) and 0x1FL).toInt() + 1
    val totalSamples = packed and 0xFFFFFFFFFL

    val md5 = get(16)

    return StreamInfoBlock(
      minBlockSize,
      maxBlockSize,
      minFrameSize,
      maxFrameSize,
      sampleRate,
      channels,
      bitsPerSample,
      totalSamples,
      md5,
    )

  }

  private fun Reader.parsePaddingBlock(length: Int): PaddingBlock {
    skip(length)
    return PaddingBlock(length)
  }

  private fun Reader.parsePictureBlock(): PictureBlock {

    val pictureType = getInteger32().let {
      PictureType(it)
    }

    val mimeType = run {
      val length = getInteger32()
      get(length).toString(StandardCharsets.UTF_8)
    }.let {
      MimeType(it)
    }

    val description = run {
      val length = getInteger32()
      get(length).toString(StandardCharsets.UTF_8)
    }.let {
      Description(it)
    }

    val width = getInteger32().let {
      Width(it)
    }

    val height = getInteger32().let {
      Height(it)
    }

    val depth = getInteger32().let {
      Depth(it)
    }

    val colors = getInteger32().let {
      Colors(it)
    }

    val pictureLength = getInteger32()
    val picture = slice(pictureLength, current()).let {
      Picture(it)
    }

    skip(pictureLength)

    return PictureBlock(
      pictureType,
      mimeType,
      description,
      width,
      height,
      depth,
      colors,
      picture
    )

  }

  private fun Reader.parseVorbisCommentBlock(): VorbisCommentBlock {

    val vendor = run {
      val length = getInteger32().reverseBytes()
      get(length).toString(StandardCharsets.UTF_8)
    }

    val store: MutableList<Pair<String, String>> = ArrayList()

    val count = getInteger32().reverseBytes()

    repeat(count) {

      val item = run {
        val length = getInteger32().reverseBytes()
        get(length).toString(StandardCharsets.UTF_8)
      }

      val index = item.indexOf('=')

      val itemKey = item.substring(0, index)
      val itemValue = item.substring(index + 1).let { if (enableVendorTrim) it.trim() else it }

      store.add(itemKey to itemValue)
    }

    return VorbisCommentBlocks.getInstance(vendor, store)

  }

  //= ==================================================================================================================

  override fun Reader.sliceContent(): FlacStream {
    TODO("")
  }

  override fun Reader.sliceContent(streamInfoBlock: StreamInfoBlock): FlacStream {

    val flacBitReader = FlacBitReader(this)

    var samples = 0

    var begin = -1
    var end = -1

    while (true) {

      val position = nextSync()

      if (position < 0) break
      val frameInfo = confirmHeader(position)

      if (frameInfo == null) {
        if (DEBUG) println("FRAME - SYNC $position / FALSE (Header Invalid)")
        continue
      }

      if (begin < 0) begin = position

      val success = flacBitReader.skipFrameBody(frameInfo)

      if (success) {
        end = current()
        if (DEBUG) println("FRAME - SYNC $position ~ $end skip=${end - position} true")
      } else {
        if (DEBUG) println("FRAME - SYNC $position / FALSE (Body Parse Failed)")
        break
      }

    }

    if (begin < 0 || end < 0) error("No frame found")

    if (DEBUG) System.err.println("$begin -> $end")

    return (end - begin).let {
      slice(it, begin)
    }.let {
      FlacStream(it)
    }

  }

  private fun Reader.nextSync(): Int {

    var previous = -1

    while (remaining() > 0) {

      val byte = getInteger8().toInt() and 0xFF

      // 14-bit sync code: 1111 1111 1111 10xx -> 0xFF 0xF8~0xFB
      if (previous == 0xFF && (byte and 0xFC) == 0xF8) {
        return current() - 2
      }

      previous = byte
    }

    return -1

  }

  private fun Reader.confirmHeader(syncStart: Int): FrameInfo? {

    if (remaining() < 2) return null

    val byte3 = getInteger8().toInt() and 0xFF // block size code + sample rate code
    val byte4 = getInteger8().toInt() and 0xFF // channel assignment + sample size + reserved

    val blockSizeCode = byte3 shr 4 and 0x0F

    var customBlockSize = 0

    when (blockSizeCode) {
      6 -> {
        if (remaining() < 1) return null
        customBlockSize = getInteger8().toInt() and 0xFF
      }
      7 -> {
        if (remaining() < 2) return null
        customBlockSize = getInteger16().toInt() and 0xFFFF
      }
    }

    val sampleRateCode = byte3 and 0x0F

    val channelAssignment = (byte4 shr 4) and 0x0F
    if (channelAssignment > 10) return null

    val sampleSizeCode = (byte4 shr 1) and 0x07
    if (sampleSizeCode == 3 || sampleSizeCode == 7) return null

    when (sampleRateCode) {
      12 -> if (remaining() < 1) return null else skip(1)
      13, 14 -> if (remaining() < 2) return null else skip(2)
    }

    val reserved = byte4 and 0x01
    if (reserved != 0) return null

    if (remaining() < 1) return null

    val codedNumber = getInteger8().toInt() and 0xFF

    val extra = when {
      codedNumber and 0x80 == 0x00 -> 0
      codedNumber and 0xE0 == 0xC0 -> 1
      codedNumber and 0xF0 == 0xE0 -> 2
      codedNumber and 0xF8 == 0xF0 -> 3
      codedNumber and 0xFC == 0xF8 -> 4
      codedNumber and 0xFE == 0xFC -> 5
      codedNumber == 0xFE -> 6
      else -> return null
    }

    if (remaining() < extra) return null

    skip(extra)

    if (remaining() < 1) return null

    val storedCRC = getInteger8().toInt() and 0xFF
    val crcEnd = current() - 1
    val crc = get(crcEnd - syncStart, syncStart).crc8()

    if (storedCRC != crc) {
      if (enableThrowWhenFalseSync) error("CRC $crc $storedCRC $syncStart $crcEnd")
      return null
    }

    return FrameInfo(
      blockSizeCode,
      sampleRateCode,
      channelAssignment,
      sampleSizeCode,
      codedNumber,
      customBlockSize
    )
  }

  private fun FlacBitReader.skipFrameBody(info: FrameInfo): Boolean {

    val blockSize = when (info.blockSizeCode) {
      0 -> return false
      1 -> 192
      in 2..5 -> 576 shl (info.blockSizeCode - 2)
      6, 7 -> info.customBlockSize + 1
      in 8..15 -> 256 shl (info.blockSizeCode - 8)
      else -> return false
    }

    val channels = when (info.channelAssignment) {
      in 0..7 -> info.channelAssignment + 1
      8, 9, 10 -> 2
      else -> return false
    }

    val bitsPerSample = when (info.sampleSizeCode) {
      0 -> return false
      1 -> 8
      2 -> 12
      4 -> 16
      5 -> 20
      6 -> 24
      else -> return false
    }

    repeat(channels) { channelIndex ->

      if (reader.remaining() <= 0) return false

      // 必须按位读取 subframe header
      val zeroBitPadding = getBit()
      if (zeroBitPadding != 0) return false

      var subframeType = 0

      repeat(6) {
        subframeType = (subframeType shl 1) or getBit()
      }

      val wastedBitsFlag = getBit()

      val wastedBits = if (wastedBitsFlag == 1) {
        unary() + 1
      } else {
        0
      }

      // 侧声道需要多 1 bit 处理
      var currentSampleBits = bitsPerSample
      if (info.channelAssignment == 8 && channelIndex == 1) currentSampleBits++
      if (info.channelAssignment == 9 && channelIndex == 0) currentSampleBits++
      if (info.channelAssignment == 10 && channelIndex == 1) currentSampleBits++

      val validSampleBits = currentSampleBits - wastedBits
      if (validSampleBits <= 0) return false

      when (subframeType) {

        // Constant subframe
        0 -> {
          skipBits(validSampleBits)
        }

        // Verbatim subframe
        1 -> {
          skipBits(blockSize * validSampleBits)
        }

        // Fixed predictor
        in 0b001000..0b001100 -> {
          val order = subframeType and 0x07
          skipBits(order * validSampleBits)
          skipResidual(blockSize, order)
        }

        // LPC predictor
        in 0b100000..0b111111 -> {
          val order = (subframeType and 0x1F) + 1
          skipBits(order * validSampleBits)

          var precisionField = 0
          repeat(4) { precisionField = (precisionField shl 1) or getBit() }
          if (precisionField == 0b1111) return false
          val coefficientPrecision = precisionField + 1

          skipBits(5) // LPC shift
          skipBits(order * coefficientPrecision) // LPC coefficients

          skipResidual(blockSize, order)

        }

        else -> return false

      }

    }

    // 读完所有 Subframe，将 bitBuffer 对齐到字节
    byteAlign()

    // 跳过 2 字节 CRC-16
    if (reader.remaining() < 2) return false
    reader.skip(2)

    return true
  }

  private fun FlacBitReader.skipResidual(blockSize: Int, order: Int) {

    // 2 bits: Residual method
    var method = 0
    repeat(2) {
      method = (method shl 1) or getBit()
    }

    // 4 bits: Partition order
    var partitionOrder = 0
    repeat(4) {
      partitionOrder = (partitionOrder shl 1) or getBit()
    }

    val partitions = 1 shl partitionOrder

    repeat(partitions) { p ->

      // Parameter bit width depends on method
      val paramBits = if (method == 0) {
        4
      } else {
        5
      }

      var riceParam = 0

      repeat(paramBits) {
        riceParam = (riceParam shl 1) or getBit()
      }

      val escapeCode = if (method == 0) 15 else 31

      // Partition 0 需要减去 warm-up order
      val samplesInPartition = if (partitionOrder == 0) {
        blockSize - order
      } else if (p == 0) {
        (blockSize / partitions) - order
      } else {
        blockSize / partitions
      }

      if (riceParam == escapeCode) {

        // Unencoded escape
        var escapeBits = 0

        repeat(5) {
          escapeBits = (escapeBits shl 1) or getBit()
        }

        skipBits(samplesInPartition * escapeBits)
      } else {

        // Rice encoded residual
        repeat(samplesInPartition) {
          unary()
          skipBits(riceParam)
        }

      }

    }
  }

  //= ==================================================================================================================

  private data class FrameInfo(
    val blockSizeCode: Int,
    val sampleRateCode: Int,
    val channelAssignment: Int,
    val sampleSizeCode: Int,
    val codedNumber: Int,
    val customBlockSize: Int, // 新增：保存自定义块大小
  )

  class FlacBitReader(val reader: Reader) {

    var bitCount: Int = 0
    var bitBuffer: Int = 0

    fun getBit(): Int {
      if (bitCount == 0) {
        if (reader.remaining() <= 0) error("FLAC bit reader exhausted at byte ${reader.current()}")
        bitBuffer = reader.getInteger8().toInt() and 0xFF
        bitCount = 8
      }
      val bit = (bitBuffer ushr 7) and 1
      bitBuffer = bitBuffer shl 1
      bitCount--
      return bit
    }

    fun skipBits(nBits: Int) {
      var n = nBits
      if (bitCount > 0) {
        val take = minOf(bitCount, n)
        bitBuffer = bitBuffer shl take
        bitCount -= take
        n -= take
      }
      if (n >= 8) {
        val bytes = n / 8
        if (reader.remaining() < bytes) error("FLAC bit reader exhausted at byte ${reader.current()}")
        reader.skip(bytes)
        n -= bytes * 8
      }
      repeat(n) { getBit() }
    }

    fun unary(): Int {
      var count = 0
      while (true) {
        val b = getBit()
        if (b == 1) break
        count++
      }
      return count
    }

    fun byteAlign() {
      bitCount = 0
      bitBuffer = 0
    }

  }

  //= ==================================================================================================================

  private fun Reader.readU24(): Int = ((getInteger8().toInt() and 0xFF) shl 16) or ((getInteger8().toInt() and 0xFF) shl 8) or (getInteger8().toInt() and 0xFF)

  private fun ByteArray.crc8(): Int {
    var crc = 0
    forEach {
      crc = crc xor (it.toInt() and 0xFF)
      repeat(8) {
        crc = if (crc and 0x80 != 0) {
          (crc shl 1) xor 0x07
        } else {
          crc shl 1
        }
        crc = crc and 0xff
      }
    }
    return crc
  }

}