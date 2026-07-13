@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package top.btswork.ncmtoolkit.libncm.ncm.impl

import top.btswork.ncmtoolkit.libncm.LibNcm
import top.btswork.ncmtoolkit.libncm.ncm.core.ContentKeyRaw
import top.btswork.ncmtoolkit.libncm.ncm.core.ContentRaw
import top.btswork.ncmtoolkit.libncm.ncm.core.MetadataRaw
import top.btswork.ncmtoolkit.libncm.ncm.core.NcmParser
import top.btswork.ncmtoolkit.libncm.ncm.core.NcmProcessorFactory
import top.btswork.ncmtoolkit.libncm.ncm.core.PictureRaw
import top.btswork.ncmtoolkit.libncm.ncm.impl.AES.decrypt
import top.btswork.ncmtoolkit.libncm.ncm.impl.AES.toAesCipher
import top.btswork.ncmtoolkit.libncm.ncm.impl.RC4.rc4PRGA
import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.reverseBytes
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class NcmProcessorFactoryImpl : NcmProcessorFactory {
  override fun getParser(): NcmParser = NcmProcessorImpl()
}

class NcmProcessorImpl : NcmParser() {

  override fun Reader.checkMagicImpl(): Boolean {
    // 8 字节 魔数
    val magicHead = get(8)
    return LibNcm.MAGIC.contentEquals(magicHead)
  }

  override fun Reader.parseContentKeyImpl(): ContentKeyRaw {

    // 2 字节 垃圾
    skip(2)

    // 4 字节 长度
    val contentKeyLength = getInteger32().reverseBytes()

    // N 字节 内容
    return get(contentKeyLength).map {
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

  override fun Reader.parseMetadataImpl(): MetadataRaw {

    // 4 字节 长度
    val metadataLength = getInteger32().reverseBytes()

    // N 字节 内容
    return get(metadataLength).map {
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

  override fun Reader.parsePictureImpl(): PictureRaw {

    // 9 字节 垃圾
    skip(9)

    // 4 字节 长度
    val pictureLength = getInteger32().reverseBytes()

    // N 字节 内容
    return get(pictureLength).let {
      PictureRaw(it)
    }

  }

  override fun Reader.parseContentImpl(contentKey: ContentKeyRaw): ContentRaw {

    val contentLength = remaining()

    // 剩余所有字节
    return get(contentLength).also {
      contentKey.value.rc4PRGA(it)
    }.let {
      ContentRaw(it)
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

