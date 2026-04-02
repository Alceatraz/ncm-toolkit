@file:Suppress("unused")

package top.btswork.ncmtoolkit.libncm.core

import top.btswork.ncmtoolkit.libncm.schema.NcmMetadata
import java.nio.ByteBuffer

interface NcmProcessorFactory {
  fun getParser(): NcmParser
  fun getMetadataParser(): NcmMetadataParser
  fun getByteBufferContext(buffer: ByteBuffer): NcmContext
}

@JvmInline value class ContentKeyRaw(val value: ByteArray)
@JvmInline value class MetadataRaw(val value: ByteArray)
@JvmInline value class PictureRaw(val value: ByteArray)
@JvmInline value class ContentRaw(val value: ByteArray)

abstract class NcmParser {

  fun checkMagic(ctx: NcmContext) = ctx.checkMagicImpl()
  fun parseContentKey(ctx: NcmContext) = ctx.parseContentKeyImpl()
  fun parseMetadata(ctx: NcmContext) = ctx.parseMetadataImpl()
  fun parsePicture(ctx: NcmContext) = ctx.parsePictureImpl()
  fun parseContent(ctx: NcmContext, contentKey: ContentKeyRaw) = ctx.parseContentImpl(contentKey)

  protected abstract fun NcmContext.checkMagicImpl(): Boolean
  protected abstract fun NcmContext.parseContentKeyImpl(): ContentKeyRaw
  protected abstract fun NcmContext.parseMetadataImpl(): MetadataRaw
  protected abstract fun NcmContext.parsePictureImpl(): PictureRaw
  protected abstract fun NcmContext.parseContentImpl(contentKey: ContentKeyRaw): ContentRaw

}

@DslMarker annotation class NcmProcessorDsl

@NcmProcessorDsl interface NcmContext {
  fun remaining(): Int
  fun readIntLE(): Int
  fun skip(length: Int)
  fun copy(length: Int): ByteArray
}

interface NcmMetadataParser {
  fun parse(content: MetadataRaw): NcmMetadata
}