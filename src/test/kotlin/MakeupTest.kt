import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import top.btswork.ncmtoolkit.libncm.ncm.core.MetadataRaw
import top.btswork.ncmtoolkit.libncm.ncm.schema.toMetadata
import top.btswork.ncmtoolkit.tool.io.fs.Recursive
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration.ofSeconds
import kotlin.io.path.name
import kotlin.random.Random

class MakeupTest {

  @Test
  fun test() {

    val cache = Path.of("""C:\Temp\NCM\cache""")
    val pathJson = cache.resolve("json")

    val input = mutableListOf<Path>()

    val recursive = Recursive.getPathRecursive(pathJson) {
      it.name.endsWith(".json")
    }

    recursive.iterate(input::add)

    val client: HttpClient = HttpClient.newBuilder()
      .connectTimeout(ofSeconds(5))
      .build()

    runBlocking {

      for (path in input) {

        val bytes = Files.readAllBytes(path)
        val metadata = MetadataRaw(bytes).toMetadata()

        val picId = metadata.albumPicDocId

        if (metadata.albumPic.value.endsWith(".jpg").not()) {
          error("NOT JPG - " + metadata.albumPic.value)
        }

        val picturePath = cache.resolve(picId.value + ".jpg")

        if (Files.exists(picturePath)) {
          println("SKIP - ${picId.value}")
          continue
        }

        val request = HttpRequest.newBuilder()
          .uri(URI.create(metadata.albumPic.value))
          .timeout(ofSeconds(10))
          .GET()
          .build()

        val response: HttpResponse<ByteArray?> = client.send(
          request,
          HttpResponse.BodyHandlers.ofByteArray()
        ) ?: error("RESPONSE NULL")

        val body = response.body() ?: error("RESPONSE BODY NULL")

        println("DOWNLOAD ${picId.value} size=${body.size}")

        Files.createFile(picturePath)
        Files.write(picturePath, body)

        delay(Random.nextLong(1000, 5000))

      }

    }

  }

}