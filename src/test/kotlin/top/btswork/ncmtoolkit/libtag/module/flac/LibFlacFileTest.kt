package top.btswork.ncmtoolkit.libtag.module.flac

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

      /*      blocks.value.forEach {

              when (it) {
                is VorbisCommentBlock -> {
                  println("VB VENDOR " + it.vendor)
                  it.store.forEach { pair ->
                    println("${pair.first}: ${pair.second}")
                  }
                }
                else -> {}
              }

            }*/

      val flacStream = reader.sliceContent()

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

