@file:Suppress("unused")

package top.btswork.ncmtoolkit.libncm.ncm.core

import top.btswork.ncmtoolkit.tool.io.stream.Reader

interface NcmProcessorFactory {
  fun getParser(): NcmParser
}

@JvmInline value class ContentKeyRaw(val value: ByteArray)
@JvmInline value class MetadataRaw(val value: ByteArray)
@JvmInline value class PictureRaw(val value: ByteArray)
@JvmInline value class ContentRaw(val value: ByteArray)

abstract class NcmParser {

  fun Reader.checkMagic() = checkMagicImpl()
  fun Reader.parseContentKey() = parseContentKeyImpl()
  fun Reader.parseMetadata() = parseMetadataImpl()
  fun Reader.parsePicture() = parsePictureImpl()
  fun Reader.parseContent(contentKey: ContentKeyRaw) = parseContentImpl(contentKey)

  protected abstract fun Reader.checkMagicImpl(): Boolean
  protected abstract fun Reader.parseContentKeyImpl(): ContentKeyRaw
  protected abstract fun Reader.parseMetadataImpl(): MetadataRaw
  protected abstract fun Reader.parsePictureImpl(): PictureRaw
  protected abstract fun Reader.parseContentImpl(contentKey: ContentKeyRaw): ContentRaw

}
