# R8 optimization rules

-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# === Gson ===
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.internal.LinkedTreeMap {
    java.util.Set entrySet();
}
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class com.google.gson.internal.$Gson$Types { *; }
-keep class com.google.gson.internal.$Gson$Preconditions { *; }
-keep class com.google.gson.internal.$Gson$Types$GenericArrayTypeImpl { *; }
-keep class com.google.gson.internal.$Gson$Types$ParameterizedTypeImpl { *; }
-keep class com.google.gson.internal.$Gson$Types$WildcardTypeImpl { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken {
    <init>();
}

# === Model classes (must keep all for Gson reflection) ===
-keep class hai.qstory.plugin.manager.data.** { *; }

# === Retrofit service ===
-keep interface hai.qstory.plugin.manager.network.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# === Kotlin coroutines (Retrofit suspend support) ===
-keep class kotlin.coroutines.Continuation { *; }

# === Kotlin ===
-keep class kotlin.Metadata { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === Kotlin Serialization ===
-keep class hai.qstory.plugin.manager.**$$serializer { *; }
-keepclassmembers class hai.qstory.plugin.manager.** {
    *** Companion;
}
-dontwarn kotlinx.serialization.**

# === Navigation3 ===
-keep class hai.qstory.plugin.manager.Route { *; }
-keep class hai.qstory.plugin.manager.Route$** { *; }

# === Compose runtime ===
-keep class androidx.compose.runtime.** { *; }

# === Libraries (just dontwarn) ===
-dontwarn com.materialkolor.**
-dontwarn coil.**
-dontwarn top.yukonga.miuix.**
