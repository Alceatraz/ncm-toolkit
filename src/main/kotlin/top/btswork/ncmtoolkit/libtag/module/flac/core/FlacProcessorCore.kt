package top.btswork.ncmtoolkit.libtag.module.flac.core

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import top.btswork.ncmtoolkit.libtag.module.flac.schema.Block
import java.nio.ByteBuffer

interface FlacProcessorFactory {
  fun getReader(): FlacReader
  fun getWriter(): FlacWriter
}

interface FlacReader {
  fun Reader.checkMagic(): Boolean
  fun Reader.parseBlocks(): List<Block>
  fun Reader.sliceContent(): ByteBuffer
}

interface FlacWriter {
  fun Writer.save(audioFile: FlacAudioFile)
}

class FlacAudioFile(
  val blocks: List<Block>,
  val content: ByteBuffer,
)
