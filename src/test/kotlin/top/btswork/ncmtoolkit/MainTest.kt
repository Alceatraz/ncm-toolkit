package top.btswork.ncmtoolkit

import kotlin.test.Test

class MainTest {

  // parse --cache "C:\Temp\NCM\cache" --input "C:\Temp\NCM\input" --input "C:\Users\netuser\Music\App-NE\VipSongsDownload" --output "C:\Temp\NCM\output"

  @Test
  fun test01() {
    val args = arrayOf(
      "extract",
      "--cache", """C:\Temp\NCM\cache""",
      // "--input", """C:\Temp\NCM\input""",
      "--input", """C:\Users\netuser\Music\App-NE\VipSongsDownload""",
      "--output", """C:\Temp\NCM\output"""
    )
    main(args)
  }

  @Test
  fun test02() {
    val args = arrayOf(
      "fetch",
      "--cache", """C:\Temp\NCM\cache""",
      // "--input", """C:\Temp\NCM\input""",
      "--input", """C:\Users\netuser\Music\App-NE\VipSongsDownload""",
      "--output", """C:\Temp\NCM\output"""
    )
    main(args)
  }

}