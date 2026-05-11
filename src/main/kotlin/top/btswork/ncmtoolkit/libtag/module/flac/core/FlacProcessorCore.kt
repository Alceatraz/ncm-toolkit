package top.btswork.ncmtoolkit.libtag.module.flac.core

import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.Block
import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.StreamInfoBlock
import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.ByteBuffer

interface FlacProcessorFactory {
  fun getReader(): FlacReader
  fun getWriter(): FlacWriter
}

interface FlacReader {
  fun Reader.checkMagic(): Boolean
  fun Reader.parseBlocks(): FlacBlocks
  fun Reader.sliceContent(): FlacStream
  fun Reader.sliceContent(streamInfoBlock: StreamInfoBlock): FlacStream
}

interface FlacWriter {
  fun Writer.save(flacFile: FlacFile)
  fun Writer.save(flacBlocks: FlacBlocks, flacStream: FlacStream)
}

data class FlacFile(
  val flacBlocks: FlacBlocks,
  val flacStream: FlacStream,
)

@JvmInline value class FlacBlocks(
  val value: List<Block>,
)

@JvmInline value class FlacStream(
  val value: ByteBuffer,
)