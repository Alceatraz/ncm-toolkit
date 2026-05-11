package top.btswork.ncmtoolkit.libtag.module.mp3.impl

import top.btswork.ncmtoolkit.libtag.module.mp3.LibMp3
import top.btswork.ncmtoolkit.libtag.module.mp3.core.DEBUG
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Body
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Head
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Reader
import top.btswork.ncmtoolkit.tool.io.stream.Reader

class Mp3ReaderImpl : Mp3Reader {

  private val v22 = Mp3ReaderV22Impl()
  private val v23 = Mp3ReaderV23Impl()
  private val v24 = Mp3ReaderV24Impl()

  override fun Reader.checkMagicHeader(): Boolean =
    // 0 ~ 2 ID3
    get(3) contentEquals LibMp3.ID3V2_MAGIC

  override fun Reader.parseHead(): Mp3Head {

    // 3 version
    // 4 revision

    val version = getInteger8()
    val revision = getInteger8()

    if (DEBUG) println("ID3 = $version.$revision")

    return when (version.toInt()) {
      2 -> with(v22) { parse() }
      3 -> with(v23) { parse() }
      4 -> with(v24) { parse() }
      else -> TODO("Unknown MP3 version $version")
    }

  }

  override fun Reader.parseBody(lookahead: Int): Mp3Body {
    TODO("Not yet implemented")
  }

}
