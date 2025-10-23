plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // id("kotlin-kapt")  // <- fuera por ahora
}

android {
    namespace = "com.vistamed.mgp.vistamedmvp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vistamed.mgp.vistamedmvp"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { isMinifyEnabled = false }
    }

    buildFeatures { viewBinding = true }

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/INDEX.LIST",
            "META-INF/*.kotlin_module"
        )
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    // UI
    implementation("com.google.android.material:material:1.11.0")

    // Lifecycle + Coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ===================================================================
    // TensorFlow Lite

    // Para el pre-procesamiento de imagen (ImageProcessor)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // Para el intérprete (Interpreter) y el delegado NNAPI (NnApiDelegate)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // Para el delegado GPU (GpuDelegate)
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    // ===================================================================

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}