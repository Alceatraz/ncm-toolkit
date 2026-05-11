package top.btswork.ncmtoolkit.libtag.module.flac.schema.block

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

@JvmInline value class PictureType(val value: Int)
@JvmInline value class MimeType(val value: String)
@JvmInline value class Description(val value: String)
@JvmInline value class Width(val value: Int)
@JvmInline value class Height(val value: Int)
@JvmInline value class Depth(val value: Int)
@JvmInline value class Colors(val value: Int)
@JvmInline value class Picture(val value: ByteBuffer)

object PictureBlocks {

  fun getInstance(
    mime: String,
    picture: ByteBuffer,
  ) = PictureBlock(
    PictureType(3),
    MimeType(mime),
    Description(""),
    Width(0),
    Height(0),
    Depth(0),
    Colors(0),
    Picture(picture),
  )

  fun Reader.parse(): PictureBlock {
    TODO()
  }

  fun Writer.write(block: PictureBlock) {

  }

}

data class PictureBlock(
  val pictureType: PictureType,
  val mimeType: MimeType,
  val description: Description,
  val width: Width,
  val height: Height,
  val depth: Depth,
  val colors: Colors,
  val picture: Picture,
) : Block {
  override fun getType(): Byte = PICTURE
}
