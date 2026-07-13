@file:Suppress("unused")

package top.btswork.ncmtoolkit.tool.json

import java.math.BigDecimal
import java.math.BigInteger

object Json {

  fun parse(text: String): JsonReader = JsonParser(text).parse().let { JsonReader(it) }

  sealed class JsonElement {

    data class Obj(val value: Map<String, JsonElement>) : JsonElement()
    data class Arr(val value: List<JsonElement>) : JsonElement()

    data class Str(val value: String) : JsonElement()

    sealed class Num : JsonElement() {
      data class Integer(val value: BigInteger) : Num()
      data class Decimal(val value: BigDecimal) : Num()
    }

    data class Bool(val value: Boolean) : JsonElement()

    object Null : JsonElement()

    fun stringify(): String = when (this) {
      is Obj -> value.entries.joinToString(prefix = "{", postfix = "}") { """"${it.key}":${it.value.stringify()}""" }
      is Arr -> value.joinToString(prefix = "[", postfix = "]") { it.stringify() }
      is Str -> """"${escapeString(value)}""""
      is Num.Integer -> value.toString()
      is Num.Decimal -> value.toPlainString()
      is Bool -> if (value) "true" else "false"
      Null -> "null"
    }

    private fun escapeString(s: String): String = buildString {
      for (c in s) {
        when (c) {
          '\\' -> append("\\\\")
          '"' -> append("\\\"")
          '\b' -> append("\\b")
          '\u000C' -> append("\\f")
          '\n' -> append("\\n")
          '\r' -> append("\\r")
          '\t' -> append("\\t")
          else -> {
            if (c < ' ') {
              append("\\u")
              append(c.code.toString(16).padStart(4, '0'))
            } else append(c)
          }
        }
      }
    }
  }

  class JsonReader(val node: JsonElement) {

    operator fun get(key: String): JsonReader {
      val obj = node as? JsonElement.Obj
        ?: error("Not an object, cannot access key '$key'")
      val value = obj.value[key]
        ?: error("Key '$key' not found in object")
      return JsonReader(value)
    }

    operator fun get(index: Int): JsonReader {
      val arr = node as? JsonElement.Arr
        ?: error("Not an array, cannot access index $index")
      val value = arr.value.getOrNull(index)
        ?: error("Index $index out of bounds (size=${arr.value.size})")
      return JsonReader(value)
    }

    val obj: Map<String, JsonReader>
      get() = (node as? JsonElement.Obj)?.value?.mapValues { JsonReader(it.value) }
        ?: error("Expected object but was $node")

    val arr: List<JsonReader>
      get() = (node as? JsonElement.Arr)?.value?.map(::JsonReader)
        ?: error("Expected array but was $node")

    val string: String
      get() = (node as? JsonElement.Str)?.value
        ?: error("Expected string but was $node")

    val boolean: Boolean
      get() = (node as? JsonElement.Bool)?.value
        ?: error("Expected boolean but was $node")

    val integer: BigInteger
      get() = (node as? JsonElement.Num.Integer)?.value
        ?: error("Expected integer but was $node")

    val decimal: BigDecimal
      get() = (node as? JsonElement.Num.Decimal)?.value
        ?: error("Expected decimal but was $node")

    val isNull: Boolean
      get() = node is JsonElement.Null

    val value: String
      get() = when (node) {
        is JsonElement.Obj -> error("Cannot convert object to string")
        is JsonElement.Arr -> error("Cannot convert array to string")
        is JsonElement.Str -> node.value
        is JsonElement.Bool -> node.value.toString()
        is JsonElement.Num.Integer -> node.value.toString()
        is JsonElement.Num.Decimal -> node.value.toPlainString()
        JsonElement.Null -> "null"
      }

  }

  class JsonParser(private val s: String) {

    private var i = 0

    fun parse(): JsonElement {
      skip()
      return when (peek()) {
        '"' -> JsonElement.Str(parseString())
        '{' -> parseObject()
        '[' -> parseArray()
        '-', in '0'..'9' -> parseNumber()
        else -> parseLiteral()
      }
    }

    private fun parseLiteral(): JsonElement {
      return when {
        s.startsWith("null", i) -> {
          i += 4
          JsonElement.Null
        }
        s.startsWith("true", i) -> {
          i += 4
          JsonElement.Bool(true)
        }
        s.startsWith("false", i) -> {
          i += 5
          JsonElement.Bool(false)
        }
        else -> error("Unexpected literal at position $i")
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

    private fun parseNumber(): JsonElement.Num {
      val start = i
      if (s[i] == '-') i++
      if (i >= s.length || !s[i].isDigit()) error("Invalid number at position $start")
      if (s[i] == '0') {
        i++
        if (i < s.length && s[i].isDigit()) {
          error("Leading zeros are not allowed at position $start")
        }
      } else {
        while (i < s.length && s[i].isDigit()) i++
      }
      if (i < s.length && s[i] == '.') {
        i++
        if (i >= s.length || !s[i].isDigit()) error("Invalid decimal at position $start")
        while (i < s.length && s[i].isDigit()) i++
      }
      if (i < s.length && (s[i] == 'e' || s[i] == 'E')) {
        i++
        if (i < s.length && (s[i] == '+' || s[i] == '-')) i++
        if (i >= s.length || !s[i].isDigit()) error("Invalid exponent at position $start")
        while (i < s.length && s[i].isDigit()) i++
      }
      val text = s.substring(start, i)
      return if (text.contains('.') || text.contains('e', true)) {
        JsonElement.Num.Decimal(BigDecimal(text))
      } else {
        JsonElement.Num.Integer(BigInteger(text))
      }
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