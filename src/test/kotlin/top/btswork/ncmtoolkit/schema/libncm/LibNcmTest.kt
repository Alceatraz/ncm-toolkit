package top.btswork.ncmtoolkit.schema.libncm

import top.btswork.ncmtoolkit.libncm.LibNcm
import top.btswork.ncmtoolkit.tool.io.stream.Readers
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.test.Test

class LibNcmTest {

  @Test
  fun test0() {

    /* val path = Paths.get("""C:\Temp\NCM\input\test1.ncm""")
     val channel = FileChannel.open(path, StandardOpenOption.READ)
     val byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())

     val parser = LibNcm.getInstance()
     val context = LibNcm.getByteBufferContext(byteBuffer)

     parser.withContext(context)

     require(parser.checkMagic()) {
       "Not a NCM"
     }

     val parseContentKey = parser.parseContentKey()
     // println(parseContentKey.value.contentToString())

     val parseMetadata = parser.parseMetadata()
     //    println(String(parseMetadata.value))

     val parsePicture = parser.parsePicture()
     Files.newOutputStream(Paths.get("$path.jpg")).use {
       it.write(parsePicture.value)
     }

     val parseContent = parser.parseContent(parseContentKey)
     Files.newOutputStream(Paths.get("$path.flac")).use {
       it.write(parseContent.value)
     }

     val metadata = LibNcm.getMetadataParser().parse(parseMetadata)
     println(metadata)*/

  }

  @Test
  fun test1() {
    val path = Paths.get("""C:\Users\netuser\Music\App-NE\VipSongsDownload\Infant Annihilator,Storm Strope\The Battle of Yaldabaoth\Ov Sacrament And Sincest.ncm""")
    val channel = FileChannel.open(path, StandardOpenOption.READ)
    val byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
    val reader = Readers.getInstance(byteBuffer)
    val (metadata, picture, content) = LibNcm.parse(reader)
    println(metadata)
  }

  @Test
  fun test02() {

    val comment =
      """L64FU3W4YxX3ZFTmbZ+8/RPZHruwUjUPdhHhy7T7yEzjnXXHBS06PxggkhjbJMNEOBxW6EleqztNoKNkgP7fsOm6m6/CEYpmuqwvRwIXyQ5YTkDt9hOJKUpr+akepE7mHFwLe9MSxr6bYffrsi58+eP94D/+GZq4Xg+5KcIlpQ6o2GtJ35n45rXZX1TylJ23+YdxzKJ6pXB6Zz0BecLTwtBpDmgi7VyqIp1b6DuUhRpz1Rsnsmlqc0kkhKIj18LhKIXVPBkiZ8WrKJOHnZZRGV5AdkpRAEBIrIohOO+hXFUrvvejOALu4Lm7UlYx5+I4aGqCMZl5Oj/DCavhL3Eu6/fvDTebUKG0+lwpRWKhoGp0xjUyAhIv0WQjcT8tT9AB7mXmgNjp7VV89zkjXnbBiGAfeDA4NGy3xKbTbdYdv2yiu8MqFpIMhu0ibvLZO9oZp3vHUh1CR8wiD34/LfrKcanHfM/e2ny+DEcVdcm1ZZHFcqIJoI6/h1D7b6h0/pcyLqkXkgTRI12G4hmf3mu/rNmlvrMMt3NkA8sGBshGmhlMR+0DcZgGiqt8pbfJVqm87ANhgQFqqFMZeBjlqhKlp/P/TPXh4MwcyVsik4lQG+I91w8XwewoFWiVyJn/8D4dhzbI1eXKqG1EQl4bSD9WoA=="""

    //    val base64 = Codec.base64(comment.toByteArray())
    //    val metaData = Codec.aes(base64, META_KEY)
    //    val json = metaData.copyOfRange(6, metaData.size)
    //
    //    println(String(json))

  }
}