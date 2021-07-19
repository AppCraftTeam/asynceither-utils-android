package pro.appcraft.either

// fun <R> UseCaseResult<R>.toEither(): AsyncCatching<R> = data
//     ?.let { AsyncEither.Right(it) }
//     ?: error?.let { AsyncEither.Left(it) }
//     ?: AsyncEither.Left(Exception("Unexpected exception in UseCaseResult: data=null and error=null"))
