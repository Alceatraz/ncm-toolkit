package top.btswork.ncmtoolkit.app.asserts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers.ofByteArray
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration.ofSeconds
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class PictureProvider(
  private val cacheImage: Path,
) {

  private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

  private val scope = CoroutineScope(SupervisorJob() + dispatcher)

  private val cache = ConcurrentHashMap<String, ByteArray>()
  private val future = ConcurrentHashMap<String, CompletableFuture<Optional<ByteArray>>>()

  private val client = HttpClient.newBuilder()
    .connectTimeout(ofSeconds(5))
    .build()

  fun load(id: String, url: String): Optional<ByteArray> {

    if (cache.containsKey(id)) {
      println(">> CACHE $id")
      return Optional.of(cache[id]!!)
    }
    if (future.containsKey(id)) {
      println(">> DEFER $id")
      return future[id]!!.get()
    }

    val imagePath = cacheImage.resolve("$id.jpg")

    if (Files.exists(imagePath)) {
      val data = Files.readAllBytes(imagePath)
      println("[PICS] LOCAL size=${data.size} $id - $url")
      return Optional.of(data)
    }

    val completableFuture: CompletableFuture<Optional<ByteArray>> = CompletableFuture.supplyAsync {
      System.err.println(">> FETCH $id")
      val result = download(url)
      if (result.isPresent) {
        val data = result.get()
        Files.createFile(imagePath)
        Files.write(imagePath, data)
        cache[id] = data
        future.remove(id)
        println("[PICS] FETCH size=${data.size} $id - $url")
      }
      result
    }

    future[id] = completableFuture

    return completableFuture.get()
  }

  private val downloadExecutor = Executors.newSingleThreadExecutor()

  private fun download(url: String): Optional<ByteArray> {
    return downloadExecutor.submit<Optional<ByteArray>> {
      val result = fetch(url)
      Thread.sleep((750..2500).random().toLong())
      result
    }.get()
  }

  private fun fetch(url: String): Optional<ByteArray> {
    val request = HttpRequest.newBuilder().GET()
      .uri(URI.create(url))
      .timeout(ofSeconds(10))
      .build()
    // println("GET ${request.uri()}")
    val response = client.send(request, ofByteArray())?.body()
    return Optional.ofNullable(response)
  }

}





