plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // id("kotlin-kapt")  // <- fuera por ahora
}

android {
    namespace = "com.vistamed.mgp.vistamedmvp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vistamed.mgp.vistamedmvp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
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

    // TensorFlow Lite - La librería Task Vision ya incluye las dependencias necesarias del core y de support.
    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")

    // Opcional: Delegado de GPU para aceleración por hardware
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
