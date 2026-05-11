package top.btswork.ncmtoolkit.libtag.module.flac.schema.block

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

object BasicBlocks {

  fun getInstance(
    blockType: Byte,
    blockPayload: ByteBuffer,
  ) = BasicBlock(
    blockType,
    blockPayload
  )

  fun Reader.parse(): BasicBlock {
    TODO()
  }

  fun Writer.write(block: BasicBlock) {

  }

}

data class BasicBlock(
  val blockType: Byte,
  val blockPayload: ByteBuffer,
) : Block {
  override fun getType() = blockType
}