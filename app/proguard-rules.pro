# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Google API Client
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }
-dontwarn com.google.api.**
-dontwarn org.apache.http.**
-dontwarn com.google.common.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
