@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package top.btswork.ncmtoolkit.libncm.impl

import top.btswork.ncmtoolkit.tool.reverseBytes
import top.btswork.ncmtoolkit.libncm.LibNcm
import top.btswork.ncmtoolkit.libncm.core.ContentKeyRaw
import top.btswork.ncmtoolkit.libncm.core.ContentRaw
import top.btswork.ncmtoolkit.libncm.core.MetadataRaw
import top.btswork.ncmtoolkit.libncm.core.NcmContext
import top.btswork.ncmtoolkit.libncm.core.NcmMetadataParser
import top.btswork.ncmtoolkit.libncm.core.NcmParser
import top.btswork.ncmtoolkit.libncm.core.NcmProcessorFactory
import top.btswork.ncmtoolkit.libncm.core.PictureRaw
import top.btswork.ncmtoolkit.libncm.impl.AES.decrypt
import top.btswork.ncmtoolkit.libncm.impl.AES.toAesCipher
import top.btswork.ncmtoolkit.libncm.impl.RC4.rc4PRGA
import top.btswork.ncmtoolkit.libncm.schema.NcmMetadata
import top.btswork.ncmtoolkit.libncm.schema.toAlbum
import top.btswork.ncmtoolkit.libncm.schema.toAlbumId
import top.btswork.ncmtoolkit.libncm.schema.toAlbumPic
import top.btswork.ncmtoolkit.libncm.schema.toAlbumPicDocId
import top.btswork.ncmtoolkit.libncm.schema.toAlias
import top.btswork.ncmtoolkit.libncm.schema.toArtist
import top.btswork.ncmtoolkit.libncm.schema.toArtistId
import top.btswork.ncmtoolkit.libncm.schema.toArtistName
import top.btswork.ncmtoolkit.libncm.schema.toBitrate
import top.btswork.ncmtoolkit.libncm.schema.toDuration
import top.btswork.ncmtoolkit.libncm.schema.toFee
import top.btswork.ncmtoolkit.libncm.schema.toFormat
import top.btswork.ncmtoolkit.libncm.schema.toMp3DocId
import top.btswork.ncmtoolkit.libncm.schema.toMusicId
import top.btswork.ncmtoolkit.libncm.schema.toMusicName
import top.btswork.ncmtoolkit.libncm.schema.toMvId
import top.btswork.ncmtoolkit.libncm.schema.toPrivilege
import top.btswork.ncmtoolkit.libncm.schema.toTransNames
import top.btswork.ncmtoolkit.libncm.schema.toVolumeDelta
import top.btswork.ncmtoolkit.libncm.tool.NcmJson
import top.btswork.ncmtoolkit.libncm.tool.NcmJson.stringify
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class NcmProcessorFactoryImpl : NcmProcessorFactory {
  override fun getParser(): NcmParser = NcmProcessorImpl()
  override fun getMetadataParser(): NcmMetadataParser = NcmMetadataParserImpl()
  override fun getByteBufferContext(buffer: ByteBuffer): NcmContext = ByteBufferNcmContext(buffer.slice())
}

class NcmProcessorImpl : NcmParser() {

  override fun NcmContext.checkMagicImpl(): Boolean {
    // 8 字节 魔数
    val magicHead = copy(8)
    return LibNcm.MAGIC.contentEquals(magicHead)
  }

  override fun NcmContext.parseContentKeyImpl(): ContentKeyRaw {

    // 2 字节 垃圾
    skip(2)

    // 4 字节 长度
    val contentKeyLength = readIntLE()

    // N 字节 内容
    return copy(contentKeyLength).map {
      it xor 0x64
    }.toByteArray().let {
      LibNcm.CORE_KEY.toAesCipher().decrypt(it)
    }.let {
      it.copyOfRange(17, it.size)
    }.let {
      RC4.rc4KSA(it)
    }.let {
      ContentKeyRaw(it)
    }
  }

  override fun NcmContext.parseMetadataImpl(): MetadataRaw {

    // 4 字节 长度
    val metadataLength = readIntLE()

    // N 字节 内容
    return copy(metadataLength).map {
      it xor 0x63
    }.toByteArray().let {
      it.copyOfRange(22, it.size)
    }.let {
      Base64.getDecoder().decode(it)
    }.let {
      LibNcm.META_KEY.toAesCipher().decrypt(it)
    }.let {
      it.copyOfRange(6, it.size)
    }.let {
      MetadataRaw(it)
    }

  }

  override fun NcmContext.parsePictureImpl(): PictureRaw {

    // 9 字节 垃圾
    skip(9)

    // 4 字节 长度
    val pictureLength = readIntLE()

    // N 字节 内容
    return copy(pictureLength).let {
      PictureRaw(it)
    }

  }

  override fun NcmContext.parseContentImpl(contentKey: ContentKeyRaw): ContentRaw {

    val contentLength = remaining()

    // 剩余所有字节
    return copy(contentLength).also {
      contentKey.value.rc4PRGA(it)
    }.let {
      ContentRaw(it)
    }
  }

}

private class ByteBufferNcmContext(
  private val buffer: ByteBuffer,
) : NcmContext {

  private val limit = buffer.limit()

  override fun remaining(): Int = buffer.remaining()

  override fun readIntLE(): Int {
    require(4 < buffer.remaining()) {
      "ByteBuffer remain content less then 4 bytes ( 1 int )"
    }
    return buffer.getInt().reverseBytes()
  }

  override fun skip(length: Int) {
    val newPosition = buffer.position() + length
    require(newPosition < limit) {
      "ByteBuffer limit is $limit but newPosition is $newPosition"
    }
    buffer.position(newPosition)
  }

  override fun copy(length: Int): ByteArray {
    require(length <= buffer.remaining()) {
      "ByteBuffer remain ${buffer.remaining()} less then $length"
    }
    return ByteArray(length).also {
      buffer.get(it)
    }
  }

}

private object RC4 {

  fun rc4KSA(key: ByteArray) = ByteArray(256).also {
    val size = key.size
    for (i in 0..255) {
      it[i] = i.toByte()
    }
    var j = 0
    for (i in 0..255) {
      j = (j + it[i] + key[i % size]) and 255
      val swap = it[i]
      it[i] = it[j]
      it[j] = swap
    }
  }

  fun ByteArray.rc4PRGA(data: ByteArray) = data.also {
    val k = ByteArray(256)
    for (i in 0..255) {
      k[i] = this[this[i] + this[i + this[i] and 255] and 255]
    }
    for (j in it.indices) {
      it[j] = (it[j].toInt() xor k[(j + 1) % 256].toInt()).toByte()
    }
  }

}

private object AES {

  fun ByteArray.toAesCipher() =
    Cipher.getInstance("AES/ECB/PKCS5Padding").also {
      val key = SecretKeySpec(this, "AES")
      it.init(Cipher.DECRYPT_MODE, key)
    }!!

  fun Cipher.decrypt(data: ByteArray) = doFinal(data)!!

}

class NcmMetadataParserImpl : NcmMetadataParser {

  override fun parse(content: MetadataRaw): NcmMetadata {
    val content = String(content.value, StandardCharsets.UTF_8)

    // System.err.println(content)

    val json = NcmJson.parse(content)
    return NcmMetadata {
      musicId = json["musicId"].str.toMusicId()
      musicName = json["musicName"].str.toMusicName()
      artist = json["artist"].arr
        .map {
          it.arr
        }.map {
          val name = it[0].str.toArtistName()
          val id = it[1].str.toArtistId()
          id to name
        }.map {
          it.toArtist()
        }.toTypedArray()
      albumId = json["albumId"].str.toAlbumId()
      album = json["album"].str.toAlbum()
      albumPicDocId = json["albumPicDocId"].str.toAlbumPicDocId()
      albumPic = json["albumPic"].str.toAlbumPic()
      bitrate = json["bitrate"].str.toBitrate()
      mp3DocId = json["mp3DocId"].str.toMp3DocId()
      duration = json["duration"].str.toDuration()
      mvId = json["mvId"].str.toMvId()
      alias = try {
        json["alias"].arr.map {
          it.str.toAlias()
        }.toTypedArray()
      } catch (_: Exception) {
        arrayOf()
      }
      transNames = json["transNames"].arr.map {
        it.str.toTransNames()
      }.toTypedArray()
      format = json["format"].str.toFormat()
      fee = json["fee"].str.toFee()
      volumeDelta = json["volumeDelta"].str.toVolumeDelta()
      privilege = json["privilege"].node.stringify().toPrivilege()
    }
  }

}
