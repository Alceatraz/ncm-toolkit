package filter

import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class FilterTest {

  /*
  ffmpeg -i input.wav -map_channel 0.0.0 left.wav
  ffmpeg -i input.wav -map_channel 0.0.1 right.wav
  */

  val inL: Path = Paths.get("""C:\Temp\SINC\music-l.pcm""")
  val inR: Path = Paths.get("""C:\Temp\SINC\music-r.pcm""")

  fun Path.read(): ShortArray {
    val bytes = Files.readAllBytes(this)
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val samples = ShortArray(bytes.size / 2)
    for (i in samples.indices) {
      samples[i] = buffer.short
    }
    return samples
  }

  fun Path.write(data: ShortArray) {
    val bb = ByteBuffer.allocate(data.size * 2).order(ByteOrder.LITTLE_ENDIAN)
    for (s in data) bb.putShort(s)
    Files.write(this, bb.array())
  }

  @Test
  fun test1() {

    val outL = Paths.get("""C:\Temp\SINC\temp-l-iir.pcm""")
    val outR = Paths.get("""C:\Temp\SINC\temp-r-iir.pcm""")

    Files.deleteIfExists(outL)
    Files.deleteIfExists(outR)

    Files.createFile(outL)
    Files.createFile(outR)

    outL.write(inL.read().iir())
    outR.write(inR.read().iir())

    // ffmpeg -f s16le -ar 88200 -ac 1 -i music-l-iir.pcm -f s16le -ar 88200 -ac 1 -i music-r-iir.pcm -filter_complex "[0:a][1:a]join=inputs=2:channel_layout=stereo" temp-iir.wav

  }

  fun ShortArray.iir(factor: Int = 2): ShortArray {
    val out = ShortArray(size * factor)
    for (i in 0 until size - 1) {
      val a = this[i].toInt()
      val b = this[i + 1].toInt()
      out[i * factor] = a.toShort()
      out[i * factor + 1] = ((a + b) / 2).toShort()
    }
    return out
  }

  @Test
  fun test2() {

    val inL = Paths.get("""C:\Temp\SINC\music-l.pcm""")
    val inR = Paths.get("""C:\Temp\SINC\music-r.pcm""")

    val outL = Paths.get("""C:\Temp\SINC\temp-l-fir.pcm""")
    val outR = Paths.get("""C:\Temp\SINC\temp-r-fir.pcm""")

    Files.deleteIfExists(outL)
    Files.deleteIfExists(outR)

    Files.createFile(outL)
    Files.createFile(outR)

    val h = generateRectangularFIR(512, 0.40)

    val a = System.nanoTime()

    outL.write(inL.read().fir(h))
    outR.write(inR.read().fir(h))

    val b = System.nanoTime()

    println((b - a) / 1000000.0)

    //    val cutL = Paths.get("""C:\Temp\SINC\temp-l-fir-cut.pcm""")
    //    val cutR = Paths.get("""C:\Temp\SINC\temp-r-fir-cut.pcm""")
    //
    //    val y = x.firFilter(h)      // 过滤后
    //    val d = ShortArray(x.size)
    //
    //    for (i in x.indices) {
    //      d[i] = (x[i] - y[i]).toShort()
    //    }

    // ffmpeg -f s16le -ar 44100 -ac 1 -i temp-l-fir.pcm -f s16le -ar 44100 -ac 1 -i temp-r-fir.pcm -filter_complex "[0:a][1:a]join=inputs=2:channel_layout=stereo" temp-fir.wav

  }

  fun ShortArray.fir(h: DoubleArray): ShortArray {
    val out = ShortArray(this.size)
    val taps = h.size
    for (i in indices) {
      var acc = 0.0
      for (k in 0 until taps) {
        val idx = i - k
        if (idx >= 0) acc += this[idx] * h[k]
      }
      out[i] = acc.toInt().coerceIn(-32768, 32767).toShort()
    }
    return out
  }

  fun generateLinearPhaseFIR(taps: Int, cutoff: Double): DoubleArray {
    val h = generateFIR(taps, cutoff) { n, M ->
      // Blackman-Harris
      0.35875 -
          0.48829 * cos(2 * Math.PI * n / M) +
          0.14128 * cos(4 * Math.PI * n / M) -
          0.01168 * cos(6 * Math.PI * n / M)
    }
    // 强制对称
    val M = taps - 1
    for (i in 0 until taps / 2) {
      h[M - i] = h[i]
    }
    return h
  }

  fun generateRectangularFIR(taps: Int, cutoff: Double): DoubleArray = generateFIR(taps, cutoff, ::rectangularWindow)
  fun generateChebyshevFIR(taps: Int, cutoff: Double, rippleDb: Double = 60.0): DoubleArray = generateFIR(taps, cutoff) { n, M -> chebyshevWindow(n, M, rippleDb) }

  fun rectangularWindow(n: Int, M: Int): Double = 1.0

  fun chebyshevWindow(n: Int, M: Int, rippleDb: Double = 60.0): Double {
    val tg = 10.0.pow(rippleDb / 20.0)
    val beta = cosh(1.0 / M * acosh(tg))
    val x = beta * cos(Math.PI * (2.0 * n - M) / M)
    return 1.0 / cosh(M * acosh(x))
  }

  // 自己实现一个 acosh
  fun acosh(x: Double): Double {
    // 这里假设 x >= 1（切比雪夫窗里就是这个范围）
    return ln(x + sqrt(x * x - 1.0))
  }

  fun generateFIR(taps: Int, cutoff: Double, window: (Int, Int) -> Double): DoubleArray {
    val h = DoubleArray(taps)
    val M = taps - 1
    for (n in 0 until taps) {
      val x = n - M / 2.0
      // sinc
      val sinc = if (x == 0.0) {
        2 * cutoff
      } else {
        sin(2 * Math.PI * cutoff * x) / (Math.PI * x)
      }
      // window
      val w = window(n, M)
      h[n] = sinc * w
    }
    return h
  }

  @Test
  fun test3() {
    val inL = Paths.get("""C:\Temp\SINC\music-l.pcm""")
    val inR = Paths.get("""C:\Temp\SINC\music-r.pcm""")
    val outL = Paths.get("""C:\Temp\SINC\temp-l-src.pcm""")
    val outR = Paths.get("""C:\Temp\SINC\temp-r-src.pcm""")
    Files.deleteIfExists(outL)
    Files.deleteIfExists(outR)
    Files.createFile(outL)
    Files.createFile(outR)
    outL.write(inL.read().src())
    outR.write(inR.read().src())
  }

  /*
  ffmpeg -f s16le -ar 48000 -ac 1 -i temp-l-src.pcm -f s16le -ar 48000 -ac 1 -i temp-r-src.pcm -filter_complex "[0:a][1:a]join=inputs=2:channel_layout=stereo" temp-src.wav
  */

  // 最临近 + CLAMP + 取整抖动
  fun ShortArray.src(): ShortArray {
    val ratio = 48000.0 / 44100.0
    val outSize = (this.size * ratio).toInt()
    val out = ShortArray(outSize)
    for (i in 0 until outSize) {
      val srcIndex = (i / ratio).toInt()
      val idx = srcIndex.coerceIn(0, this.size - 1)
      out[i] = this[idx]
    }
    return out
  }

}

