package top.btswork.ncmtoolkit.libtag.module.flac

import org.junit.jupiter.api.Test
import top.btswork.ncmtoolkit.libtag.module.flac.schema.VorbisCommentBlock
import top.btswork.ncmtoolkit.tool.io.stream.core.ByteBufferReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.readBytes

class LibFlacTest {

  @Test
  fun test01() {

    println("TEST01")

    val bytes = Paths.get("""C:\Temp\NCM\test1.ncm.flac""").readBytes()
    val buffer = ByteBuffer.wrap(bytes)
    val reader = ByteBufferReader(buffer)

    val flacReader = LibFlac.getReader()

    with(flacReader) {

      require(reader.checkMagic()) {
        "Not FLAC file"
      }

      val blocks = reader.parseBlocks()

      blocks.forEach {

        when (it) {

          is VorbisCommentBlock -> {
            println(it.vendor)
            it.store.forEach { pair ->
              println("${pair.first}: ${pair.second}")
            }

          }

          else -> {

          }
        }

      }

      val content = reader.sliceContent()

      val output = Paths.get("""C:\Temp\NCM\test-rewrite.flac""")

      Files.deleteIfExists(output)
      Files.createFile(output)

      FileChannel.open(output, StandardOpenOption.WRITE).use {
        it.write(content)
      }

    }

  }

}