plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)   // Firebase
    id("kotlin-parcelize")   // ← NUEVO: para @Parcelize en DteScanResult
}

android {
    namespace = "cl.nexo.empresas"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.nexo.empresas"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Lee credenciales de local.properties
        val localProps = com.android.build.gradle.internal.cxx.configure.gradleLocalProperties(rootDir, providers)
        buildConfigField("String", "SUPABASE_URL",  "\"${localProps.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProps.getProperty("SUPABASE_ANON_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
configurations.all {
    resolutionStrategy {
        force("androidx.browser:browser:1.8.0")
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // WorkManager + Hilt-Work
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // Supabase
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.android)

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // UI extras
    implementation(libs.coil.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.datastore.preferences)

    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // ── Módulo Scanner PDF417 ───────────────────────────────────────────────────
// ML Kit — barcode scanning con soporte nativo PDF417 (sin modelo de descarga)
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

// CameraX — acceso a cámara en Jetpack Compose
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.camera:camera-core:1.4.1")
}
