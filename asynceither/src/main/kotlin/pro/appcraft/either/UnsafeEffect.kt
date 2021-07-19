package pro.appcraft.either

@RequiresOptIn(
    message = "This method may produce unsafe side effects. Make sure to handle them properly before using it",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class UnsafeEffect
