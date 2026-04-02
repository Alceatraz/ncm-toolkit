package top.btswork.ncmtoolkit.app.asserts

import java.nio.file.Paths
import kotlin.test.Test

class PictureProviderTest {

  @Test
  fun test() {

    val provider = PictureProvider(Paths.get("""C:\Temp\NCM\cache\coverRemote"""))

    val data = provider.load(
      "109951166887388958",
      "http://p4.music.126.net/em2jwX9Z-pz3E9fHtojtvg==/109951166887388958.jpg"
    )

    println(data.isPresent)

  }

}