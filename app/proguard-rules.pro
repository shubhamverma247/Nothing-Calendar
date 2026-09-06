# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Hilt
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class *

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-keep @kotlinx.serialization.Serializable class * { *; }

# Glance
-keep class androidx.glance.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# QR share/scanner Compose entry points. Keep the generated file class and its composable
# singleton barrier intact; release R8 previously merged this path before the Play lab NPE.
-keep class com.dotfield.dotcal.ui.QrEventScreensKt { *; }
-keep class com.dotfield.dotcal.ui.ComposableSingletons$QrEventScreensKt { *; }

# ML Kit discovers these registrars by manifest class name and reflection. Keep their
# no-argument constructors; R8 removed them in the pre-launch artifact.
-keep class com.google.mlkit.common.internal.CommonComponentRegistrar { <init>(); }
-keep class com.google.mlkit.vision.barcode.internal.BarcodeRegistrar { <init>(); }
-keep class com.google.mlkit.vision.common.internal.VisionCommonRegistrar { <init>(); }
