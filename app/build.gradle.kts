plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ch.fhnw.pitchdetection.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "ch.fhnw.pitchdetection.demo"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // TensorFlow Lite
    implementation(libs.tensorflow.lite) // Check for the latest version
    // TensorFlow Lite GPU delegate (optional, for GPU acceleration)
    implementation(libs.tensorflow.lite.gpu.delegate.plugin) // Or the version matching your TFLite version
    implementation(libs.tensorflow.lite.gpu)
    // TensorFlow Lite NNAPI delegate (optional, for NPU acceleration)
    implementation(libs.tensorflow.lite.support) // Provides NNAPI delegate support utilities

    // For Audio processing (TensorFlow Lite Audio Task Library can be helpful)
    implementation(libs.tensorflow.lite.task.audio) // Check for the latest version

    // Other standard AndroidX libraries
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout) // If using ConstraintLayout with XML
    // ...
}