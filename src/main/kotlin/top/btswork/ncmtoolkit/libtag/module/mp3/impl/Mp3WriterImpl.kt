package top.btswork.ncmtoolkit.libtag.module.mp3.impl

import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Body
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3File
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Head
import top.btswork.ncmtoolkit.libtag.module.mp3.core.Mp3Writer
import top.btswork.ncmtoolkit.tool.io.stream.Writer

class Mp3WriterImpl : Mp3Writer {

  override fun Writer.save(file: Mp3File) {
    save(file.head, file.body)
  }

  override fun Writer.save(head: Mp3Head, body: Mp3Body) {
    TODO("Not yet implemented")
  }

}