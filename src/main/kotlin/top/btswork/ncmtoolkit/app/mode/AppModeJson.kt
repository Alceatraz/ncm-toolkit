package top.btswork.ncmtoolkit.app.mode

import top.btswork.ncmtoolkit.ApplicationContext
import top.btswork.ncmtoolkit.app.AppMode
import top.btswork.ncmtoolkit.tool.io.fs.Recursive
import top.btswork.ncmtoolkit.tool.ifNot
import top.btswork.ncmtoolkit.libengine.TaskBinding
import top.btswork.ncmtoolkit.libengine.TaskEngines
import top.btswork.ncmtoolkit.libengine.TaskScope
import top.btswork.ncmtoolkit.libengine.TaskUnit
import top.btswork.ncmtoolkit.libncm.LibNcm
import top.btswork.ncmtoolkit.libncm.core.MetadataRaw
import top.btswork.ncmtoolkit.libncm.core.NcmContext
import top.btswork.ncmtoolkit.libncm.core.NcmMetadataParser
import top.btswork.ncmtoolkit.libncm.core.NcmParser
import top.btswork.ncmtoolkit.libncm.schema.NcmMetadata
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

object AppModeJson : AppMode {

  override fun execute(context: ApplicationContext) {
    Scope(context).execute()
  }

  private fun Scope.execute() {

    val taskEngine = TaskEngines.getEngineBuilder().apply {

      withContext("NCM_PARSER") {
        LibNcm.getInstance()
      }

      withContext("NCM_META_PARSER") {
        LibNcm.getMetadataParser()
      }

      withInitial("NCM_CONTEXT")

      withBinding("ncm-check-magic") {
        requires = setOf(
          "INPUT",
          "NCM_PARSER",
          "MP3_PARSER",
          "FLAC_PARSER",
        )
        produces = setOf(
          "TYPE"
        )
        task = object : TaskUnit {

          override fun TaskScope.execute() {
            TODO("Not yet implemented")
          }

        }
      }


      withBinding(NcmCheckMagic.binding)
      withBinding(NcmParseContentKey.binding)
      withBinding(NcmParseMetadata.binding)
      withBinding(NcmConvertMetadata.binding)
      withBinding(NcmParsePicture.binding)

    }.build()

    val dirs = mutableListOf<Path>()

    for (it in input) {
      Recursive.getPathRecursive(it).iterate(dirs::add)
    }

    for (dir in dirs) {

      val builder = taskEngine.builder()

      val buffer = run {
        val channel = FileChannel.open(dir, StandardOpenOption.READ)
        channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
      }

      builder["NCM_CONTEXT"] = LibNcm.getByteBufferContext(buffer)

      val result = taskEngine.execute(builder)

      val metadata = result.getOrThrow<NcmMetadata>("NCM_METADATA")


      println("[SAVE] ${metadata.musicId.value} - ${metadata.albumPicDocId.value}")

      val jsonPath = cacheJson.resolve(metadata.musicId.value + ".json")

      Files.deleteIfExists(jsonPath)
      Files.createFile(jsonPath)

      val metadataRaw = result.getOrThrow<MetadataRaw>("NCM_METADATA_RAW")

      Files.write(jsonPath, metadataRaw.value)

      //      val imagePath = cacheCoverLocal.resolve(metadata.albumPicDocId.value + ".jpg")
      //
      //      Files.deleteIfExists(imagePath)
      //      Files.createFile(imagePath)
      //
      //      val pictureRaw = result.getOrThrow<PictureRaw>("PICTURE_LOCAL")
      //
      //      Files.write(imagePath, pictureRaw.value)

    }

  }

}

private class Scope(
  context: ApplicationContext,
) {

  val cache: Path
  val cacheJson: Path
  //  val cacheCoverLocal: Path
  //  val cacheCoverRemote: Path

  val input: Array<Path>

  init {

    require(context.config.getArgs().isNotEmpty()) {
      "Arguments cannot be empty"
    }

    input = context.config.getArgs().map {
      Paths.get(it)
    }.filter {
      Files.exists(it)
    }.toTypedArray()

    require(input.isNotEmpty()) {
      "Arguments has no valid input"
    }

    require(context.config.hasParameter("cache")) {
      "Parameter cache must be specified"
    }

    cache = Paths.get(context.config.getParameter("cache"))

    cacheJson = cache.resolve("json")
    //    cacheCoverLocal = cache.resolve("cover-local")
    //    cacheCoverRemote = cache.resolve("cover-remote")

    Files.exists(cache).ifNot { Files.createDirectories(cache) }
    Files.exists(cacheJson).ifNot { Files.createDirectories(cacheJson) }
    //    Files.exists(cacheCoverLocal).ifNot { Files.createDirectories(cacheCoverLocal) }
    //    Files.exists(cacheCoverRemote).ifNot { Files.createDirectories(cacheCoverRemote) }

  }

}

//= ============================================================================

private object Check : TaskUnit {

  override fun TaskScope.execute() {

  }

}

object NcmCheckMagic : TaskUnit {

  val binding = TaskBinding("ncm-check-magic") {
    task = NcmCheckMagic
    requires = setOf(
      "NCM_PARSER",
      "NCM_CONTEXT"
    )
    produces = setOf(
      "NCM_CHECK_MAGIC"
    )
  }

  override fun TaskScope.execute() {
    val ncmParser: NcmParser = scope["NCM_PARSER"]
    val ncmContext: NcmContext = scope["NCM_CONTEXT"]
    val checkMagic = ncmParser.checkMagic(ncmContext)
    scope["NCM_CHECK_MAGIC"] = checkMagic
    if (checkMagic.not()) throw NotNcmException()
  }

  class NotNcmException : RuntimeException()

}

object NcmParseContentKey : TaskUnit {

  val binding = TaskBinding("ncm-parse-content-key") {
    task = NcmParseContentKey
    requires = setOf(
      "NCM_PARSER",
      "NCM_CONTEXT",
      "NCM_CHECK_MAGIC"
    )
    produces = setOf(
      "NCM_CONTENT_KEY"
    )
  }

  override fun TaskScope.execute() {
    val ncmParser: NcmParser = scope["NCM_PARSER"]
    val ncmContext: NcmContext = scope["NCM_CONTEXT"]
    scope["NCM_CONTENT_KEY"] = ncmParser.parseContentKey(ncmContext)
  }

}

object NcmParseMetadata : TaskUnit {

  val binding = TaskBinding("ncm-parse-metadata") {
    task = NcmParseMetadata
    requires = setOf(
      "NCM_PARSER",
      "NCM_CONTEXT",
      "NCM_CONTENT_KEY"
    )
    produces = setOf(
      "NCM_METADATA_RAW"
    )
  }

  override fun TaskScope.execute() {
    val ncmParser: NcmParser = scope["NCM_PARSER"]
    val ncmContext: NcmContext = scope["NCM_CONTEXT"]
    scope["NCM_METADATA_RAW"] = ncmParser.parseMetadata(ncmContext)
  }
}

object NcmConvertMetadata : TaskUnit {

  val binding = TaskBinding("ncm-convert-metadata") {
    task = NcmConvertMetadata
    requires = setOf(
      "NCM_META_PARSER",
      "NCM_METADATA_RAW",
    )
    produces = setOf(
      "NCM_METADATA"
    )
  }

  override fun TaskScope.execute() {
    val ncmMetadata: MetadataRaw = scope["NCM_METADATA_RAW"]
    val ncmMetadataParser: NcmMetadataParser = scope["NCM_META_PARSER"]
    scope["NCM_METADATA"] = ncmMetadataParser.parse(ncmMetadata)
  }
}

object NcmParsePicture : TaskUnit {

  val binding = TaskBinding("ncm-parse-picture") {
    task = NcmParsePicture
    requires = setOf(
      "NCM_PARSER",
      "NCM_CONTEXT",
      "NCM_METADATA_RAW"
    )
    produces = setOf(
      "PICTURE_LOCAL"
    )
  }

  override fun TaskScope.execute() {
    val ncmParser: NcmParser = scope["NCM_PARSER"]
    val ncmContext: NcmContext = scope["NCM_CONTEXT"]
    scope["PICTURE_LOCAL"] = ncmParser.parsePicture(ncmContext)
  }

}