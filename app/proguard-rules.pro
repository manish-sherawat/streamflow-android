# Add project-specific ProGuard rules here.
-keep class com.streamflow.app.data.model.** { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keep class androidx.media3.** { *; }
