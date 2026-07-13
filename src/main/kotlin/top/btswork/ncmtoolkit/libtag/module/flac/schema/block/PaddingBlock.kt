package top.btswork.ncmtoolkit.libtag.module.flac.schema.block

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer

object PaddingBlocks {

  fun getInstance(
    size: Int,
  ) = PaddingBlock(
    size
  )

  fun Reader.parse(): PaddingBlock {
    TODO()
  }

  fun Writer.write(block: PaddingBlock) {

  }

}

data class PaddingBlock(
  val size: Int,
) : Block {

  override fun getType(): Byte = PADDING

  companion object {
    fun getInstance(size: Int) = PaddingBlock(size)
  }

}
