package top.btswork.ncmtoolkit.tool.io.stream.impl

import top.btswork.ncmtoolkit.tool.io.stream.Writer
import java.nio.channels.FileChannel

class MappedWriter(
  private val channel: FileChannel,
) : Writer by ByteBufferWriter(
  channel.map(
    FileChannel.MapMode.READ_WRITE,
    0,
    channel.size()
  ),
)