package top.btswork.ncmtoolkit.libtag.module.mp3.core

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

const val DEBUG = true

interface Mp3ProcessorFactory {
  fun getReader(): Mp3Reader
  fun getWriter(): Mp3Writer
}

interface Mp3Reader {
  fun Reader.checkMagicHeader(): Boolean
  fun Reader.parseHead(): Mp3Head
  fun Reader.parseBody(lookahead: Int = 4): Mp3Body
}

interface Mp3Writer {
  fun Writer.save(file: Mp3File)
  fun Writer.save(head: Mp3Head, body: Mp3Body)
}

sealed class ID3Tag {

  /**
   * 无法解析或不需要修改的原始帧
   */
  class ID3Raw(
    val frameId: String,
    val content: ByteArray,
  ) : ID3Tag()

  /**
   * 通用 Text 帧 (T000 - TZZZ)
   * 涵盖了 Title, Artist, Album, Year 等 90% 的标签
   */
  class ID3Text(
    val frameId: String,  // 例如 "TIT2" 或 v2.2 的 "TT2"
    val text: String,      // 自动处理编码转换后的内容
  ) : ID3Tag()

  /**
   * 用户自定义文本 (TXXX / v2.2 TXX)
   * 很多现代播放器用来存特定的 Metadata
   */
  class ID3UserText(
    val description: String,
    val value: String,
  ) : ID3Tag()

  /**
   * 封面图帧 (APIC / v2.2 PIC)
   */
  class ID3Picture(
    val mimeType: String,     // 例如 "image/jpeg"
    val pictureType: Byte,    // 0x03 是 Cover Front
    val description: String,
    val pictureData: ByteArray,
  ) : ID3Tag()

}

data class Mp3File(
  val head: Mp3Head,
  val body: Mp3Body,
)

@JvmInline value class Mp3Head(val tags: List<ID3Tag>)
@JvmInline value class Mp3Body(val segments: List<ByteBuffer>)