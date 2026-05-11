package top.btswork.ncmtoolkit.libtag.module.flac

import top.btswork.ncmtoolkit.libtag.module.flac.schema.block.StreamInfoBlock
import top.btswork.ncmtoolkit.tool.io.fs.Recursive
import top.btswork.ncmtoolkit.tool.io.stream.impl.MappedReader
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.io.path.fileSize
import kotlin.io.path.name
import kotlin.test.Test

class LibFlacFileTest {

  fun flacRead(path: Path) {

    val fileChannel: FileChannel = FileChannel.open(path, StandardOpenOption.READ)!!

    val reader = MappedReader(fileChannel)

    val flacReader = LibFlac.getReader()

    with(flacReader) {

      require(reader.checkMagic()) {
        "Not FLAC file"
      }

      val blocks = reader.parseBlocks()

      val block = blocks.value[0] as StreamInfoBlock

      val flacStream = reader.sliceContent(block)

      println("""$block - ${flacStream.value.limit()} / ${path.fileSize()}""")

    }

  }

  @Test
  fun test00() {

    val recursive = Recursive.getPathRecursive(Paths.get("""C:\Users\netuser\Music\App-NE""")) {
      it.name.endsWith("flac")
    }

    val arr = ArrayList<Path>()
    recursive.iterate { arr.add(it) }

    arr.parallelStream().forEach {

      try {
        flacRead(it)
      } catch (e: Exception) {
        System.err.println("""$it ${e.message} | ${it.fileSize()}""")
      }
    }

  }

  @Test
  fun test01() {
    val path = Paths.get("""C:\Temp\NCM\test-r.flac""")
    flacRead(path)
  }

}

