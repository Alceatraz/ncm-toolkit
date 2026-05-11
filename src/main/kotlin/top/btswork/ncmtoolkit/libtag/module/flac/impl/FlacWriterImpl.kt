package top.btswork.ncmtoolkit.libtag.module.flac.impl

import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacBlocks
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacFile
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacStream
import top.btswork.ncmtoolkit.libtag.module.flac.core.FlacWriter
import top.btswork.ncmtoolkit.tool.io.stream.Writer

class FlacWriterImpl : FlacWriter {

  override fun Writer.save(flacFile: FlacFile) {
    save(flacFile.flacBlocks, flacFile.flacStream)
  }

  override fun Writer.save(flacBlocks: FlacBlocks, flacStream: FlacStream) {

    flacBlocks.value.forEach {

    }

  }

}