package top.btswork.ncmtoolkit.core.fp

import top.btswork.ncmtoolkit.tool.fp.Expect
import top.btswork.ncmtoolkit.tool.fp.from
import top.btswork.ncmtoolkit.tool.fp.map
import top.btswork.ncmtoolkit.tool.fp.mapExpect
import top.btswork.ncmtoolkit.tool.fp.recovery
import top.btswork.ncmtoolkit.tool.fp.then
import top.btswork.ncmtoolkit.tool.fp.unwrap
import java.net.URI
import kotlin.test.Test

class ExpectTest {

  @Test
  fun test() {

    val expect = Expect.from {
      "http://localhost:8080" // 假设有副作用
    }.then {
      println("READDISK $it")
    }.map {
      URI.create(it)!!
    }.mapExpect {
      Expect.from {
        "DOWNLOAD $it".toByteArray() // 假设有副作用
      }
    }.recovery { // 上一步成功这步短路
      println("DOWNLOAD failed ${it.message}")
      "THIS IS DEFAULT VALUE".toByteArray() // 上一步如果失败 则注入默认值
    }.map {
      String(it) // 成功则继续
    }.unwrap()

    println(expect)

  }

}