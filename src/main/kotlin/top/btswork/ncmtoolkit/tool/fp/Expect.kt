@file:Suppress("unused", "NOTHING_TO_INLINE")

package top.btswork.ncmtoolkit.tool.fp

import top.btswork.ncmtoolkit.tool.fp.Expect.Failure
import top.btswork.ncmtoolkit.tool.fp.Expect.Success

sealed class Expect<out T> {

  companion object

  fun isSuccess() = this is Success
  fun isFailure() = this is Failure

  data class Success<T>(val value: T) : Expect<T>()
  data class Failure(val exception: Throwable) : Expect<Nothing>()

}

inline fun <T> Expect.Companion.from(block: () -> T): Expect<T> = try {
  Success(block())
} catch (exception: Throwable) {
  Failure(exception)
}

inline fun <T, R> Expect<T>.fold(
  onSuccess: (T) -> R,
  onFailure: (Throwable) -> R,
): R = when (this) {
  is Success -> onSuccess(value)
  is Failure -> onFailure(exception)
}

inline fun <T> Expect<T>.expect(expect: (Throwable) -> Throwable): T = when (this) {
  is Success -> value
  is Failure -> throw expect(exception)
}

inline fun <T> Expect<T>.unwrap(): T = when (this) {
  is Success -> value
  is Failure -> throw RuntimeException("Unwarp an failure", exception)
}

inline fun <T> Expect<T>.unwrapOrNull(): T? = when (this) {
  is Success -> value
  is Failure -> null
}

inline fun <T> Expect<T>.then(transform: (T) -> Unit): Expect<T> = when (this) {
  is Success -> apply { transform(value) }
  is Failure -> this
}

inline fun <T, R> Expect<T>.map(transform: (T) -> R): Expect<R> = when (this) {
  is Success -> Success(transform(value))
  is Failure -> this
}

inline fun <T, R> Expect<T>.mapExpect(transform: (T) -> Expect<R>): Expect<R> = when (this) {
  is Success -> transform(value)
  is Failure -> this
}

inline fun <T> Expect<T>.recovery(block: (Throwable) -> T): Expect<T> = when (this) {
  is Success -> this
  is Failure -> Success(block(exception))
}

inline fun <T> Expect<T>.recoveryExpect(block: (Throwable) -> Expect<T>): Expect<T> = when (this) {
  is Success -> this
  is Failure -> block(exception)
}

/*
 * Expect.from { httpGet(url) }
 *     .recoverExpect { e ->
 *         Expect.from { httpGet(backupUrl) }
 *     }
 *
 */