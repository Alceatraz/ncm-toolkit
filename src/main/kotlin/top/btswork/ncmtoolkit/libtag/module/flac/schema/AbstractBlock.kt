package top.btswork.ncmtoolkit.libtag.module.flac.schema

const val STREAMINFO = 0.toByte()
const val PADDING = 1.toByte()
const val APPLICATION = 2.toByte()
const val SEEKTABLE = 3.toByte()
const val VORBIS_COMMENT = 4.toByte()
const val CUESHEET = 5.toByte()
const val PICTURE = 6.toByte()

sealed interface Block {
  fun getType(): Byte
}


