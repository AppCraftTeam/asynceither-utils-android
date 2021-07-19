@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package pro.appcraft.either

import java.util.NoSuchElementException
import java.util.Optional

typealias AsyncCatching<T> = AsyncEither<Throwable, T>

@Suppress("unused")
sealed class AsyncEither<out L, out R> {
    // Error container
    data class Left<out L>(val error: L) : AsyncEither<L, Nothing>()

    // Result container
    data class Right<out T>(val value: T) : AsyncEither<Nothing, T>()

    val isLeft: Boolean
        get() = this is Left<L>

    val isRight: Boolean
        get() = this is Right<R>

    override fun toString(): String {
        return when (this) {
            is Left<L> -> "Either.Left[error=$error]"
            is Right<R> -> "Either.Right[value=$value]"
        }
    }

    suspend inline fun exists(crossinline predicate: suspend (R) -> Boolean): Boolean =
        this is Right<R> && predicate(value)

    // Execute on failure
    suspend inline fun <O> onLeft(crossinline f: suspend (L) -> O): AsyncEither<L, R> {
        if (this is Left<L>)
            f(error)
        return this
    }

    // Execute on success
    suspend inline fun <O> onRight(crossinline f: suspend (R) -> O): AsyncEither<L, R> {
        if (this is Right<R>)
            f(value)
        return this
    }

    suspend inline fun <O> map(crossinline f: suspend (R) -> O): AsyncEither<L, O> =
        when (this) {
            is Left<L> -> this
            is Right<R> -> Right(f(value))
        }

    suspend inline fun <M> mapLeft(crossinline f: suspend (L) -> M): AsyncEither<M, R> =
        when (this) {
            is Left<L> -> Left(f(error))
            is Right<R> -> this
        }

    // Collect result and error
    suspend inline fun <K, O> fold(
        crossinline ifRight: suspend (R) -> O,
        crossinline ifLeft: suspend (L) -> K
    ) {
        when (this) {
            is Left<L> -> ifLeft(error)
            is Right<R> -> ifRight(value)
        }
    }

    // Nullable version of get
    fun orNull(): R? = (this as? Right<R>)?.value

    // Error-throwing version of get, for using in try-catch outside of the type
    @UnsafeEffect
    fun unsafeGet(): R = when (this) {
        is Right<R> -> value
        is Left<L> -> throw (error as? Throwable) ?: NoSuchElementException("Expected Right, got Left, error=$error")
    }

    fun toOptional(): Optional<out R> = Optional.ofNullable(orNull())

    // Not available at the time
    // fun toResult(): Result<T> = runCatching { unsafeGet() }

    fun show() = toString()

    companion object {
        suspend inline fun <T> catch(crossinline supplier: suspend () -> T): AsyncCatching<T> =
            try {
                Right(supplier())
            } catch (t: Throwable) {
                Left(t)
            }

        fun <T> fromNullable(value: T?): AsyncEither<T?, T> = value?.let(::Right) ?: Left(null)

        fun <T> fromOptional(optional: Optional<T>) = fromNullable(optional.orElse(null))

        fun <T> fromResult(result: Result<T>): AsyncCatching<T> = result
            .fold(::Right, ::Left)
    }
}

suspend inline fun <L, R, O> AsyncEither<L, R>.flatMap(crossinline f: suspend (R) -> AsyncEither<L, O>): AsyncEither<L, O> =
    when (this) {
        is AsyncEither.Left<L> -> this
        is AsyncEither.Right<R> -> f(value)
    }

// flatMap for errors
suspend inline fun <L, R, M> AsyncEither<L, R>.handleErrorWith(crossinline f: suspend (L) -> AsyncEither<M, R>): AsyncEither<M, R> =
    when (this) {
        is AsyncEither.Left<L> -> f(error)
        is AsyncEither.Right<R> -> this
    }

// Transform error to success
suspend inline fun <L, R : O, O> AsyncEither<L, R>.handleError(crossinline f: suspend (L) -> O): AsyncEither<L, O> =
    when (this) {
        is AsyncEither.Left<L> -> AsyncEither.Right(f(error))
        is AsyncEither.Right<R> -> this
    }

// Transform error and get
suspend inline fun <L, R : O, O> AsyncEither<L, R>.getOrHandle(crossinline f: suspend (L) -> O): O =
    when (this) {
        is AsyncEither.Right<R> -> value
        is AsyncEither.Left<L> -> f(error)
    }

suspend fun <L, R : O, O> AsyncEither<L, R>.getOrElse(other: O): O = getOrHandle { other }

suspend fun <L, R> AsyncEither<L, R?>.leftIfNull(): AsyncEither<L?, R> =
    flatMap { a -> a?.let { b -> AsyncEither.Right(b) } ?: AsyncEither.Left(null) }

fun <L, R : O, O> AsyncEither<L, R>.contains(element: O): Boolean =
    this is AsyncEither.Right<O> && value == element

// Monoid composition
suspend inline fun <L, R, O> AsyncEither<L, suspend (R) -> O>.compose(fb: AsyncEither<L, R>): AsyncEither<L, O> =
    flatMap { a -> fb.map(a) }

suspend inline fun <L, R, O, P> AsyncEither<L, R>.zip(
    fb: AsyncEither<L, O>,
    crossinline f: suspend (R, O) -> P
): AsyncEither<L, P> = flatMap { b -> fb.map { c -> f(b, c) } }

suspend fun <L, R, O> AsyncEither<L, R>.zip(fb: AsyncEither<L, O>): AsyncEither<L, Pair<R, O>> =
    flatMap { a -> fb.map { b -> Pair(a, b) } }

// Catch error-throwing value supplier
suspend fun <T> (suspend () -> T).eitherCatching() = AsyncEither.catch(this)
