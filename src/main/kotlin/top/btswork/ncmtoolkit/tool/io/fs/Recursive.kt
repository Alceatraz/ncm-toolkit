package top.btswork.ncmtoolkit.tool.io.fs

import java.nio.file.Files
import java.nio.file.Path

object Recursive {
  fun getPathRecursive(path: Path, filter: (Path) -> Boolean = { true }) = PathRecursive(path, filter)
}

class PathRecursive(
  private val path: Path,
  private val filter: (Path) -> Boolean,
) {

  fun iterate(func: (Path) -> Unit) {
    iterate(path, func)
  }

  private fun iterate(path: Path, func: (Path) -> Unit) {
    Files.list(path).forEach {
      when {
        Files.isRegularFile(it) -> if (filter.invoke(it)) func(it)
        Files.isDirectory(it) -> iterate(it, func)
      }
    }
  }

}