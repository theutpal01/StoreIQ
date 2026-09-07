plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.storiq"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.storiq"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.lifecycle.ExperimentalLifecycleApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        viewBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    packagingOptions {
        resources {
            excludes += listOf("META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.window)

    // Compose Material 3
    implementation(enforcedPlatform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)

    // Material3 View (for XML themes)
    implementation(libs.material3.view)

    // Coil
    implementation(enforcedPlatform(libs.coil.bom))
    implementation(libs.coil.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    configurations.named("kapt") {
    dependencies.add(project.dependencies.create("androidx.room:room-compiler:2.6.1"))
}

    // DataStore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.rxjava3)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coroutines & Flow
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Material Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.data)
}

// Jacoco Coverage
tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport> {
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}

// Detekt Configuration
tasks.named<io.gitlab.arturbosch.detekt.Detekt>("detekt") {
    config = files("$projectDir/../detekt.yml")
    baseline = file("$projectDir/../detekt-baseline.xml")
    buildUponDefaultConfig = true
    reports {
        html {
            required.set(true)
            destination = file("$buildDir/reports/detekt/detekt.html")
        }
        xml {
            required.set(true)
            destination = file("$buildDir/reports/detekt/detekt.xml")
        }
        txt {
            required.set(true)
            destination = file("$buildDir/reports/detekt/detekt.txt")
        }
        sarif {
            required.set(true)
            destination = file("$buildDir/reports/detekt/detekt.sarif")
        }
    }
}

// Android Lint Configuration
android {
    lintOptions {
        isAbortOnError = false
        isCheckReleaseBuilds = true
        isWarningsAsErrors = false
        htmlReport = true
        xmlReport = true
        htmlOutput = file("$buildDir/reports/lint/lint-results.html")
        xmlOutput = file("$buildDir/reports/lint/lint-results.xml")
    }
    lint {
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
        disable += "UnusedResources"
        baseline = file("$projectDir/../../lint-baseline.xml")
    }
}

