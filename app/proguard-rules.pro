# ProGuard rules for StorIQ

# Keep Room entities
-keep class com.storiq.core.model.** { *; }

# Keep DAOs
-keep class com.storiq.core.database.** { *; }

# Keep Repository
-keep class com.storiq.core.storage.** { *; }

# Keep ViewModels
-keep class com.storiq.feature.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep AndroidX
-keep class androidx.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Datastore
-keep class androidx.datastore.** { *; }

# Keep WorkManager
-keep class androidx.work.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep DataStore Preferences
-keep class androidx.datastore.preferences.** { *; }

# Keep Kotlin reflection
-keep class kotlin.reflect.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep data classes
-keep class * {
    public <fields>;
    public <methods>;
}