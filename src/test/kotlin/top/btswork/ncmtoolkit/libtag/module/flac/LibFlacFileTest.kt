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

      //      val vorbisCommentBlock = blocks.value.filterIsInstance<VorbisCommentBlock>().first()
      //      println(vorbisCommentBlock.vendor)
      //      vorbisCommentBlock.store.forEach { pair -> println(pair.first + "=" + pair.second) }

      val block = blocks.value[0] as StreamInfoBlock

      val flacStream = reader.sliceContent(block.totalSamples)

      val actual = flacStream.value.limit().toLong()
      val fileSize = path.fileSize()

      val bodySize = fileSize - actual

      when {
        actual == fileSize -> println("""$block - $actual / $fileSize""")
        actual < fileSize -> System.err.println("""$path $block - $actual / $fileSize""")
      }
    }

  }

  //  @Test
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

  //  @Test
  fun test01() {
    val path = Paths.get("""C:\Temp\NCM\test-r.flac""")
    flacRead(path)
  }

  //  @Test
  fun test02() {

    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\Lune - Hymns To The Lunar Realm - 01 Lustrous Gates.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\Lune - Hymns To The Lunar Realm - 02 Of White Silk And Marble Pillars.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\Lune - Hymns To The Lunar Realm - 03 Stardust Elysium.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\Lune - Hymns To The Lunar Realm - 04 Hymns To The Lunar Realm.flac"""))

    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\1.01 - When Time Fades Away.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\1.02 - Sons Of Winter And Stars.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\1.03 - Land Of Snow And Sorrow.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\1.04 - Darkness And Frost.flac"""))
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\1.05 - Time.flac"""))
  }

  @Test
  fun test03() {
    flacRead(Paths.get("""C:\Temp\NCM\bandcamp\Lune - Hymns To The Lunar Realm - 01 Lustrous Gates.flac"""))
  }

}

/**
 * C:\Users\netuser\Music\App-NE\Sabaton\Hearts of Iron IV  Sabaton Soundtrack\Primo Victoria.flac
 * StreamInfoBlock(minBlockSize=1024, maxBlockSize=1024, minFrameSize=14, maxFrameSize=3495, sampleRate=44100, channels=2, bitsPerSample=16, totalSamples=11056752,
 * md5=[120, -75, -125, -90, 68, 62, 76, 54, -44, 107, -56, -23, 78, -29, 22, 117])
 * 33548036 / 33726621
 *
 *
 *
 */