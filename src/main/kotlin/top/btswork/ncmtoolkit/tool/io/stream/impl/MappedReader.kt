package top.btswork.ncmtoolkit.tool.io.stream.impl

import top.btswork.ncmtoolkit.tool.io.stream.Reader
import top.btswork.ncmtoolkit.tool.io.stream.core.ByteBufferReader
import java.nio.channels.FileChannel

class MappedReader(
  private val channel: FileChannel,
) : Reader by ByteBufferReader(
  channel.map(
    FileChannel.MapMode.READ_ONLY,
    0,
    channel.size()
  )
)