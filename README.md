# AsyncEither
[![](https://jitpack.io/v/AppCraftTeam/asynceither-utils-android.svg)](https://jitpack.io/#AppCraftTeam/asynceither-utils-android)

Simplified implementation of [Either](https://www.ibm.com/developerworks/library/j-ft13/index.html) error handling type.\
Based on Arrow.kt Either [implementation](https://arrow-kt.io/docs/apidocs/arrow-core-data/arrow.core/-either/) with all callbacks built with `suspend` support.\
Compatible with Kotlin's `Result<T>` type.

## Installation

Add to build.gradle:
```
// Base (AsyncEither<L, R> and AsyncCatching<R> classes):
implementation 'com.github.AppCraftTeam.asynceither-utils-android:asynceither:TAG'
// Kotlin Result<T> drop-in support:
implementation 'com.github.AppCraftTeam.asynceither-utils-android:asynceither-result:TAG'
```
