# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable, *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep classes and members annotated with @Keep
-keep class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}
-keep @androidx.annotation.Keep class * { *; }


# Keep Glance SerializedName fields
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep AppWidgetProviders
-keep class com.smokingtracker.widget.** { *; }
-keepclassmembers class com.smokingtracker.widget.** { *; }

# Koin (reflection-based DI)
-keep class org.koin.** { *; }
-keep class com.smokingtracker.di.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# WorkManager instantiates workers via reflection
-keep class com.smokingtracker.notification.DailyNudgeWorker { *; }

# Gson model classes (reflection serialization)
-keep class com.smokingtracker.** { *; }

# Room entities
-keep class com.smokingtracker.data.local.** { *; }