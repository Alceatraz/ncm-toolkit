package top.btswork.ncmtoolkit.libtag.module.mp3

import org.junit.jupiter.api.Test
import top.btswork.ncmtoolkit.tool.io.stream.impl.ByteBufferReader
import java.nio.ByteBuffer
import java.nio.file.Paths
import kotlin.io.path.readBytes

class LibMp3Test {

  @Test
  fun test01() {

    println("TEST01")

    val bytes = Paths.get("""C:\Temp\NCM\test_1.mp3""").readBytes()
    val buffer = ByteBuffer.wrap(bytes)
    val reader = ByteBufferReader(buffer)

    val mp3Reader = LibMp3.getReader()

    with(mp3Reader) {

      require(reader.checkMagicHeader()) {
        "Not ID3v2 head"
      }

      val mp3Head = reader.parseHead()

    }

  }

}