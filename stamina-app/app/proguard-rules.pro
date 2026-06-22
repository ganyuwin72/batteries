# 体力 App ProGuard 规则
# DataStore
-keep class androidx.datastore.** { *; }

# Glance
-keep class androidx.glance.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
