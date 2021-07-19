@file:Suppress("unused")

package pro.appcraft.either

val <T> AsyncCatching<T>.isSuccess: Boolean get() = isRight

val <T> AsyncCatching<T>.isFailure: Boolean get() = isLeft

fun <T> AsyncCatching<T>.getOrNull(): T? = orNull()

fun <T> AsyncCatching<T>.exceptionOrNull(): Throwable? =
    (this as? AsyncEither.Left<Throwable>)?.error

@OptIn(UnsafeEffect::class)
fun <T> AsyncCatching<T>.getOrThrow(): T = unsafeGet()

suspend fun <T> AsyncCatching<T>.getOrElse(onFailure: suspend (exception: Throwable) -> T): T =
    getOrHandle(onFailure)

suspend fun <T> AsyncCatching<T>.getOrDefault(defaultValue: T): T = getOrElse(defaultValue)

suspend fun <R, T> AsyncCatching<T>.mapCatching(transform: suspend (value: T) -> R): AsyncCatching<R> =
    flatMap { a -> AsyncEither.catch { transform(a) } }

suspend fun <R, T : R> AsyncCatching<T>.recover(transform: suspend (exception: Throwable) -> R): AsyncCatching<R> =
    handleError(transform)

suspend fun <R, T : R> AsyncCatching<T>.recoverCatching(transform: (exception: Throwable) -> R): AsyncCatching<R> {
    return when (this) {
        is AsyncEither.Right<T> -> this
        is AsyncEither.Left<Throwable> -> AsyncEither.catch { transform(error) }
    }
}

suspend fun <T> AsyncCatching<T>.onFailure(action: suspend (exception: Throwable) -> Unit) =
    onLeft(action)

suspend fun <T> AsyncCatching<T>.onSuccess(action: suspend (value: T) -> Unit) = onRight(action)
