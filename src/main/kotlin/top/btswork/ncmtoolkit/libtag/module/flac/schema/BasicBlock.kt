package top.btswork.ncmtoolkit.libtag.module.flac.schema

import java.nio.ByteBuffer

object BasicBlocks {
  fun getInstance(
    blockType: Byte,
    blockPayload: ByteBuffer,
  ) = BasicBlock(
    blockType,
    blockPayload
  )
}

data class BasicBlock(
  val blockType: Byte,
  val blockPayload: ByteBuffer,
) : Block {
  override fun getType() = blockType
}