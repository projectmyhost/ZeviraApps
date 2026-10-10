# Navira ProGuard / R8 Rules

# Strip Timber logs in release build
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static void w(...);
}

# Keep WebAppInterface for Leaflet WebView Bridge
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Tink AEAD
-keep class com.google.crypto.tink.** { *; }

# Google Play Services & Credentials
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Firebase
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
