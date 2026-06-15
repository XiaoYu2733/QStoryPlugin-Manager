# R8 optimization rules for QStory Plugin Manager
# Base: proguard-android-optimize.txt

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
# If stack traces look wrong with the above, comment it out and uncomment:
# -renamesourcefileattribute SourceFile
# Required for Gson and serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# === Gson ===
# Only keep what Gson reflection actually touches
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class com.google.gson.internal.LinkedTreeMap {
    java.util.Set entrySet();
}
# Keep data classes used with Gson
-keep class hai.qstory.plugin.manager.data.** { *; }

# === Retrofit ===
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# === Kotlin ===
# R8 handles Kotlin well — only keep what reflection needs
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation
-keepclassmembers class * {
    ** serialize(..);
    ** deserialize(..);
}

# === Kotlin Serialization ===
-keepclassmembers class hai.qstory.plugin.manager.** {
    *** Companion;
}
-keep class hai.qstory.plugin.manager.**$$serializer { *; }
-dontwarn kotlinx.serialization.**

# === Compose ===
# R8 natively handles Compose — no blanket keep needed.
# Only keep Compose runtime classes accessed via reflection
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# === Navigation3 ===
-keep class hai.qstory.plugin.manager.Route { *; }
-keep class hai.qstory.plugin.manager.Route$** { *; }

# === Enums ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === MaterialKolor ===
-dontwarn com.materialkolor.**

# === Coil ===
-dontwarn coil.**

# === Miuix ===
-dontwarn top.yukonga.miuix.**
