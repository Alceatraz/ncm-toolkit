package top.btswork.ncmtoolkit.libncm.tool

object NcmJson {

  fun parse(text: String): Reader = Parser(text).parse().let { Reader(it) }

  sealed class JsonElement {
    data class Str(val value: String) : JsonElement()
    data class Arr(val value: List<JsonElement>) : JsonElement()
    data class Obj(val value: Map<String, JsonElement>) : JsonElement()
  }

  fun JsonElement.stringify(): String = when (this) {
    is JsonElement.Str -> """"$value""""
    is JsonElement.Arr -> value.joinToString(prefix = "[", postfix = "]") { it.stringify() }
    is JsonElement.Obj -> value.entries.joinToString(prefix = "{", postfix = "}") { """"${it.key}":${it.value.stringify()}""" }
  }

  class Reader(val node: JsonElement) {

    operator fun get(key: String): Reader {
      val next = when (node) {
        is JsonElement.Arr -> key.toIntOrNull()?.let { node.value.getOrNull(it) }
        is JsonElement.Obj -> node.value[key]
        else -> null
      } ?: error("Key not found: $key")
      return Reader(next)
    }

    operator fun get(index: Int): Reader {
      val arr = node as? JsonElement.Arr ?: error("Not an array")
      return Reader(arr.value.getOrNull(index) ?: error("Index out of bounds"))
    }

    val str: String get() = (node as? JsonElement.Str)?.value ?: error("Not a string")
    val arr: List<Reader> get() = (node as? JsonElement.Arr)?.value?.map { Reader(it) } ?: error("Not an array")
    val obj: Map<String, Reader> get() = (node as? JsonElement.Obj)?.value?.mapValues { Reader(it.value) } ?: error("Not an object")

  }

  class Parser(private val s: String) {

    private var i = 0

    fun parse(): JsonElement {
      skip()
      return when (peek()) {
        '"' -> JsonElement.Str(parseString())
        '{' -> parseObject()
        '[' -> parseArray()
        '-', in '0'..'9' -> JsonElement.Str(parseNumber().toString())
        else -> error("Unexpected char: ${peek()}")
      }
    }

    private fun parseObject(): JsonElement.Obj {
      expect('{')
      skip()
      val map = mutableMapOf<String, JsonElement>()
      if (peek() != '}') {
        while (true) {
          skip()
          val key = parseString()
          skip()
          expect(':')
          skip()
          val value = parse()
          map[key] = value
          skip()
          if (peek() == '}') break
          expect(',')
        }
      }
      expect('}')
      return JsonElement.Obj(map)
    }

    private fun parseArray(): JsonElement.Arr {
      expect('[')
      skip()
      val list = mutableListOf<JsonElement>()
      if (peek() != ']') {
        while (true) {
          list.add(parse())
          skip()
          if (peek() == ']') break
          expect(',')
        }
      }
      expect(']')
      return JsonElement.Arr(list)
    }

    private fun parseString(): String {
      expect('"')
      val sb = StringBuilder()
      while (i < s.length) {
        when (val c = s[i++]) {
          '"' -> return sb.toString()
          '\\' -> sb.append(parseEscape())
          else -> sb.append(c)
        }
      }
      error("Unterminated string")
    }

    private fun parseNumber(): Double {
      val start = i
      while (i < s.length && s[i] in "-+0123456789.eE") i++
      return s.substring(start, i).toDouble()
    }

    private fun parseEscape(): Char {
      if (i >= s.length) error("Bad escape")
      return when (val c = s[i++]) {
        '"', '\\', '/' -> c
        'b' -> '\b'
        'f' -> '\u000C'
        'n' -> '\n'
        'r' -> '\r'
        't' -> '\t'
        'u' -> {
          if (i + 4 > s.length) error("Bad unicode escape")
          val hex = s.substring(i, i + 4)
          i += 4
          hex.toInt(16).toChar()
        }
        else -> error("Bad escape: $c")
      }
    }

    private fun skip() {
      while (i < s.length && s[i].isWhitespace()) i++
    }

    private fun peek(): Char {
      if (i < s.length) return s[i] else error("Unexpected EOF")
    }

    private fun expect(c: Char) {
      skip()
      if (peek() != c) error("Expected '$c'")
      i++
    }
  }

}