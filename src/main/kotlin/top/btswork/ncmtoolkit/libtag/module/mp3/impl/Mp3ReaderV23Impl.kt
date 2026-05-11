package top.btswork.ncmtoolkit.libtag.module.mp3.impl

import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Head
import top.btswork.ncmtoolkit.tool.io.stream.Reader
import java.nio.ByteBuffer

class Mp3ReaderV23Impl {

  fun Reader.parse(): Mp3Head {

    //= ========================================================================
    //= header

    // 5 flag

    val flag = getInteger8().toInt() and 0xFF

    val option = Option(
      unsync = flag and 0b10000000 != 0,
      extended = flag and 0b01000000 != 0,
      experimental = flag and 0b00100000 != 0,
    )

    println("flag: $flag = unsync: ${option.unsync} extended: ${option.extended} experimental: ${option.experimental}")

    if (option.unsync) TODO("unsync")
    //    if (option.extended) TODO("extended")
    if (option.experimental) TODO("experimental")

    //= ========================================================================
    //= extend header

    // 6-9  size

    val size = getInteger32().deSynchsafe()
    val offset = size + current()

    println("SIZE = $size OFFSET $offset")

    // 10-13 ext size
    // 14-15 flag
    // 16-19 padding size

    if (option.extended) {
      TODO("extended")
    }

    // ??? ???
    // end size

    // 扩展头之后 帧

    while (current() < offset) {

      println(">>")

      val keyBytes = get(4)

      val key = String(keyBytes)
      val size = getInteger32()
      val deSize = size.deSynchsafe()

      println("KEY = $key SIZE $size / $deSize")

      val flags = getInteger16().toInt() and 0xFFFF

      val frameOption = FrameOption(
        tagAlterPreservation = flags and 0b10000000_00000000 != 0,
        fileAlterPreservation = flags and 0b01000000_00000000 != 0,
        readOnly = flags and 0b00100000_00000000 != 0,
        compression = flags and 0b10000000 != 0,
        encryption = flags and 0b01000000 != 0,
        grouping = flags and 0b00100000 != 0,
      )

      println("ABC = ${frameOption.tagAlterPreservation} / ${frameOption.fileAlterPreservation} / ${frameOption.readOnly} ")
      println("IJK = ${frameOption.compression} / ${frameOption.encryption} / ${frameOption.grouping}")

      if (frameOption.tagAlterPreservation) TODO("tagAlterPreservation")
      if (frameOption.fileAlterPreservation) TODO("fileAlterPreservation")
      if (frameOption.readOnly) TODO("readOnly")
      if (frameOption.compression) TODO("compression")
      if (frameOption.encryption) TODO("encryption")
      if (frameOption.grouping) TODO("grouping")

      val buffer = slice(size)

      val context = Context(
        option,
        frameOption,
        key,
        size,
        buffer
      )

      //      val frameDelegate = when (ID3Key.keyType(key)) {
      //        ID3KeyType.TEXT -> ID3V23TextParser()
      //        else -> TODO("UNKNOWS")
      //      }

      //      frameDelegate.attach(context).parseDelegate()

    }

    TODO()

  }

  //= ==========================================================================

  data class Context(
    val option: Option,
    val frameOption: FrameOption,
    val key: String,
    val size: Int,
    val buffer: ByteBuffer,
  )

  data class Option(
    val unsync: Boolean,
    val extended: Boolean,
    val experimental: Boolean,
  )

  data class FrameOption(
    val tagAlterPreservation: Boolean,
    val fileAlterPreservation: Boolean,
    val readOnly: Boolean,
    val compression: Boolean,
    val encryption: Boolean,
    val grouping: Boolean,
  )

}