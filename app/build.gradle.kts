plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.pokemonrandom"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pokemonrandom"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Retrofit & Gson Converter for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Image loading (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Lifecycle ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}