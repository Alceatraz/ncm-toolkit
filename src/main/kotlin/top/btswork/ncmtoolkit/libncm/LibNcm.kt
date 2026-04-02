@file:Suppress("unused")

package top.btswork.ncmtoolkit.libncm

import top.btswork.ncmtoolkit.libncm.core.ContentRaw
import top.btswork.ncmtoolkit.libncm.core.NcmContext
import top.btswork.ncmtoolkit.libncm.core.NcmProcessorFactory
import top.btswork.ncmtoolkit.libncm.core.PictureRaw
import top.btswork.ncmtoolkit.libncm.impl.NcmProcessorFactoryImpl
import top.btswork.ncmtoolkit.libncm.schema.NcmMetadata
import java.nio.ByteBuffer

object LibNcm {

  private val _MAGIC = byteArrayOf(0x43, 0x54, 0x45, 0x4E, 0x46, 0x44, 0x41, 0x4D)
  private val _CORE_KEY = byteArrayOf(0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F, 0x35, 0x6B, 0x49, 0x6E, 0x62, 0x61, 0x78, 0x57)
  private val _META_KEY = byteArrayOf(0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21, 0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28)

  val MAGIC: ByteArray
    get() = _MAGIC.clone()

  val CORE_KEY: ByteArray
    get() = _CORE_KEY.clone()

  val META_KEY: ByteArray
    get() = _META_KEY.clone()

  private var factory: NcmProcessorFactory = NcmProcessorFactoryImpl()

  fun getFactory(): NcmProcessorFactory = this.factory
  fun setFactory(factory: NcmProcessorFactory) = also { this.factory = factory }

  fun getInstance() = factory.getParser()
  fun getMetadataParser() = factory.getMetadataParser()
  fun getByteBufferContext(buffer: ByteBuffer): NcmContext = factory.getByteBufferContext(buffer)

  fun parse(byteBuffer: ByteBuffer): Triple<NcmMetadata, PictureRaw, ContentRaw> {
    val parser = factory.getParser()
    val context = factory.getByteBufferContext(byteBuffer)
    require(parser.checkMagic(context))
    val key = parser.parseContentKey(context)
    val parseMetadata = parser.parseMetadata(context)
    val picture = parser.parsePicture(context)
    val content = parser.parseContent(context, key)
    val metadata = factory.getMetadataParser().parse(parseMetadata)
    return Triple(metadata, picture, content)
  }

}

