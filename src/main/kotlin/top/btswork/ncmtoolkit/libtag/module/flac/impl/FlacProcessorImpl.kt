package top.btswork.ncmtoolkit.libtag.module.flac.impl

import top.btswork.ncmtoolkit.libtag.module.flac.LibFlac
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacAudioFile
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacProcessorFactory
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacReader
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacWriter
import top.btswork.ncmtoolkit.libtag.module.flac.schema.BasicBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Block
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Colors
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Depth
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Description
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Height
import top.btswork.ncmtoolkit.libtag.module.flac.schema.MimeType
import top.btswork.ncmtoolkit.libtag.module.flac.schema.PaddingBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Picture
import top.btswork.ncmtoolkit.libtag.module.flac.schema.PictureBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.PictureType
import top.btswork.ncmtoolkit.libtag.module.flac.schema.VorbisCommentBlock
import top.btswork.ncmtoolkit.libtag.module.flac.schema.VorbisCommentBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Width
import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import top.btswork.ncmtoolkit.tool.reverseBytes
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class FlacProcessorFactoryImpl : FlacProcessorFactory {
  override fun getReader() = FlacReaderImpl()
  override fun getWriter() = FlacWriterImpl()

}

class FlacReaderImpl : FlacReader {

  override fun Reader.checkMagic(): Boolean = LibFlac.FLAC_MAGIC contentEquals get(4)

  override fun Reader.parseBlocks(): List<Block> {

    val blocks = mutableListOf<Block>()

    while (true) {

      val head = getInteger32()
      val lastFlag = head < 0
      val blockType = head shr 24 and 0b01111111
      val blockLength: Int = head and 0b00000000_11111111_11111111_11111111

      println("${current()} -> $lastFlag: $blockType size=$blockLength")

      val block = when (blockType) {
        1 -> parsePaddingBlock()
        6 -> parsePictureBlock()
        4 -> parseVorbisCommentBlock()
        else -> {
          val blockPayload = slice(blockLength, current())
          skip(blockLength)
          BasicBlocks.getInstance(blockType.toByte(), blockPayload)
        }
      }

      blocks.add(block)

      if (lastFlag) break

    }

    return blocks

  }

  private fun Reader.parsePaddingBlock(): PaddingBlock {
    val length = getInteger32()
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

    val picture = run {
      val length = getInteger32()
      slice(length, current())
    }.let {
      Picture(it)
    }

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
      val itemValue = item.substring(index + 1).trim()

      store.add(itemKey to itemValue)
    }

    return VorbisCommentBlocks.getInstance(vendor, store)

  }

  override fun Reader.sliceContent(): ByteBuffer {

    var bitCount = 0
    var bitBuffer = 0

    val getBit = fun(): Int {
      if (bitCount == 0) {
        if (remaining() <= 0) return 0
        bitBuffer = getInteger8().toInt() and 0xFF
        bitCount = 8
      }
      val bit = bitBuffer shr 7 and 0x0000_0001
      bitBuffer = bitBuffer shl 1
      bitCount--
      return bit
    }

    val skipBit = fun(bits: Int) {
      var n = bits
      if (bitCount > 0) {
        val take = minOf(bitCount, n)
        bitBuffer = bitBuffer shl take
        bitCount -= take
        n -= take
      }
      if (n >= 8) {
        val bytes = n / 8
        if (remaining() < bytes) return
        skip(bytes)
        n -= bytes * 8
      }
      repeat(n) {
        getBit()
      }
    }

    val unary = fun(): Int {
      var count = 0
      while (true) {
        val bit = getBit()
        if (bit == 1) break
        count++
      }
      return count
    }

    var begin = -1
    var end = -1

    while (true) {
      val position = nextSync()
      if (position < 0) break

      val frameInfo = confirmHeader()

      if (frameInfo == null) {
        println("FRAME - SYNC $position / FALSE")
        continue
      }

      if (begin < 0) begin = position
      val success = skipFrameBody(getBit, skipBit, unary, frameInfo)

      end = current()

      println("FRAME - SYNC $position ~ $end skip=${end - position} $success")

    }

    if (begin < 0 || end < 0) error("No frame found")

    System.err.println("$begin -> $end ")

    return slice(current() - begin, begin)

  }

  /**
   * 15 bits  sync code = 1111 1111 1111 100
   * 1 bit    blocking strategy
   *
   * 4 bits   block size code
   * 4 bits   sample rate code
   *
   * 4 bits   channel assignment
   * 3 bits   sample size
   * 1 bit    reserved
   *
   * 8 bits   CRC-8 (header)
   */

  private fun Reader.nextSync(): Int {

    var previous = -1
    while (remaining() > 0) {
      val byte = getInteger8().toInt() and 0xFF
      if (previous == 0xFF && (byte and 0xFE) == 0xF8) {
        return current() - 2 // 返回起点
      }
      previous = byte
    }
    return -1 // no more
  }

  private fun Reader.confirmHeader(): FrameInfo? {

    if (remaining() < 2) return null

    val byte3 = getInteger8().toInt() and 0xFF // block size code + sample rate code
    val byte4 = getInteger8().toInt() and 0xFF // channel assignment + sample size + reserved

    val blockSizeCode = byte3 shr 4 and 0x0F
    // Uncommon Block Size
    when (blockSizeCode) {
      6 -> if (remaining() < 1) return null else skip(1)
      7 -> if (remaining() < 2) return null else skip(2)
    }

    val sampleRateCode = byte3 and 0x0F
    // Uncommon Sample Rate
    when (sampleRateCode) {
      12 -> if (remaining() < 1) return null else skip(1)
      13 -> if (remaining() < 2) return null else skip(2)
      14 -> if (remaining() < 2) return null else skip(2)
    }

    val channelAssignment = (byte4 shr 4) and 0x0F
    if (channelAssignment > 10) return null

    val sampleSizeCode = (byte4 shr 1) and 0x07
    if (sampleSizeCode == 3 || sampleSizeCode == 7) return null

    val reserved = byte4 and 0x01
    if (reserved != 0) return null

    // utf8 Number
    if (remaining() < 1) return null
    val codedNumber = getInteger8().toInt() and 0xFF

    val extra = when {
      codedNumber and 0x80 == 0x00 -> 0  // 1 byte
      codedNumber and 0xE0 == 0xC0 -> 1  // 2 bytes
      codedNumber and 0xF0 == 0xE0 -> 2  // 3 bytes
      codedNumber and 0xF8 == 0xF0 -> 3  // 4 bytes
      codedNumber and 0xFC == 0xF8 -> 4  // 5 bytes
      codedNumber and 0xFE == 0xFC -> 5  // 6 bytes
      codedNumber == 0xFE -> 6           // 7 bytes
      else -> return null
    }

    if (remaining() < extra) return null
    skip(extra)

    return FrameInfo(
      blockSizeCode,
      sampleRateCode,
      channelAssignment,
      sampleSizeCode,
      codedNumber
    )

  }

  private fun Reader.skipFrameBody(getBit: () -> Int, skipBit: (Int) -> Unit, unary: () -> Int, info: FrameInfo): Boolean {

    val blockSize = when (info.blockSizeCode) {
      0 -> return false
      1 -> 192
      in 2..5 -> 576 shl (info.blockSizeCode - 2)
      6 -> getInteger8().toInt() and 0xFF
      7 -> getInteger16().toInt() and 0xFFFF
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

    // 跳过 subframe
    repeat(channels) {

      if (remaining() <= 0) return false

      val subframeHeader = getInteger8().toInt() and 0xFF

      val wastedBitsFlag = subframeHeader and 1
      val subframeType = subframeHeader shr 1 and 0x3F

      val wastedBits = if (wastedBitsFlag == 1) {
        unary() + 1
      } else {
        0
      }

      /*
      0b000000	Constant subframe
      0b000001	Verbatim subframe
      0b000010 - 0b000111	Reserved
      0b001000 - 0b001100	Subframe with a fixed predictor of order v-8; i.e., 0, 1, 2, 3 or 4
      0b001101 - 0b011111	Reserved
      0b100000 - 0b111111	Subframe with a linear predictor of order v-31; i.e., 1 through 32 (inclusive)
      */

      when (subframeType) {

        // Constant subframe
        0 -> {
          // TODO: constant 子帧只有一个样本值（warm-up），然后 residual 全部为 0
          // 你需要：
          // 1) 读取一个 sample（bit-level）
          // 2) 跳过 blockSize - 1 个 residual（全部为 0）
          // 但你不解码 → 直接 skipResidual(getBit, unary, blockSize)
          TODO()
        }

        // Verbatim subframe
        1 -> {
          val totalBits = blockSize * bitsPerSample
          System.err.println("Verbatim ${current()}")
          skipBit(totalBits)
          System.err.println("Verbatim ${current()}")
        }

        // Reserved
        in 0b000010..0b000111 -> return false

        // Fixed predictor
        in 0b001000..0b001100 -> {
          // TODO:
          // order = subframeType - 8
          // 1) 跳过 warm-up samples（order 个样本，bit-level）
          // 2) 跳过 residual（blockSize - order）
          TODO()
        }

        // Reserved
        in 0b001101..0b011111 -> return false

        // LPC predictor
        in 0b100000..0b111111 -> {
          // TODO:
          // order = subframeType - 32
          // 1) 读取 LPC precision（bit-level，不是 byte-level）
          // 2) 读取 LPC shift（bit-level）
          // 3) 读取 LPC coefficients（order 个，bit-level）
          // 4) 跳过 warm-up samples（order 个样本，bit-level）
          // 5) 跳过 residual（blockSize - order）

          val order = subframeType - 32 + 1   // LPC order = 1..32

          // 1) warm-up samples
          val sampleBits = bitsPerSample - wastedBits
          if (sampleBits <= 0) return false
          skipBit(order * sampleBits)

          // 2) LPC coefficient precision (4 bits)
          var precisionField = 0
          repeat(4) { precisionField = (precisionField shl 1) or getBit() }
          if (precisionField == 0b1111) return false // reserved precision
          val coefficientPrecision = precisionField + 1

          // 3) LPC shift (5 bits)
          skipBit(5)

          // 4) LPC coefficients
          skipBit(order * coefficientPrecision)

          // 5) residual
          skipResidual(getBit, blockSize - order)

          TODO()

        }
      }

    }

    return true

  }

  private fun Reader.skipResidual(
    getBit: () -> Int,
    count: Int,
  ) {

    val start = current()

    if (remaining() <= 0) return

    // 读取 residual header（8 bits）

    val header = getInteger8().toInt() and 0xFF

    val method = header shr 6
    val partitionOrder = header and 0x3F

    // 计算 partitions

    val partitions = 1 shl partitionOrder

    // 计算 samplesPerPartition

    val samplesPerPartition = count / partitions
    if (samplesPerPartition <= 0) return

    // 遍历每个 partition


    repeat(partitions) {

      if (remaining() <= 0) return

      // 4.1 读取 Rice 参数（8 bits）
      val rice = getInteger8().toInt() and 0xFF

      val k = if (method == 0) {
        rice and 0x1F   // Rice
      } else {
        rice and 0x0F   // Rice2
      }

      // Escape code（未压缩整数）

      val escapeCode = if (method == 0) 31 else 15

      if (k == escapeCode) {
        // Rice escape：后面是未压缩整数
        // 读取 bit-width（5 bits 或 4 bits）
        val bitWidth = if (method == 0) {
          rice shr 5   // top 3 bits
        } else {
          rice shr 4   // top 4 bits
        }

        // 跳过 samplesPerPartition * bitWidth bits
        repeat(samplesPerPartition) {
          repeat(bitWidth) { getBit() }
        }

      } else {
        // -------------------------------------------------
        // 4.3 正常 Rice 编码
        // -------------------------------------------------
        repeat(samplesPerPartition) {

          // a) 跳 unary 前缀（0...01）
          while (remaining() > 0 && getBit() == 0) {
          }

          // b) 跳 k-bit 尾部
          repeat(k) { getBit() }
        }
      }
    }

    val end = current()
    println("SKIP - RESIDUAL $start - $end")
  }

  private data class FrameInfo(
    val blockSizeCode: Int,
    val sampleRateCode: Int,
    val channelAssignment: Int,
    val sampleSizeCode: Int,
    val codedNumber: Int,
  )

}

class FlacWriterImpl : FlacWriter {

  override fun Writer.save(audioFile: FlacAudioFile) {
    TODO("Not yet implemented")
  }

}