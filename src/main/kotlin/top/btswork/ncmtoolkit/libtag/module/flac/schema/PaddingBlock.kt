package top.btswork.ncmtoolkit.libtag.module.flac.schema

object PaddingBlocks {
  fun getInstance(
    size: Int,
  ) = PaddingBlock(
    size
  )
}

data class PaddingBlock(
  val size: Int,
) : Block {
  override fun getType(): Byte = PADDING

  companion object {
    fun getInstance(size: Int) = PaddingBlock(size)
  }

}