plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.pluto.adb"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pluto.adbtest"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // The library uses 1.8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8" // Or your desired Java version, matching compileOptions
    }
    buildFeatures {
        viewBinding = true // Enable ViewBinding for easier UI access
    }
}

dependencies {

// Core Android libraries (you likely have these)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // ADB Library and its dependencies
    implementation(libs.libadb.android)
    implementation(libs.sun.security.android)
    implementation(libs.hiddenapibypass)
    implementation(libs.conscrypt.android)

    // Coroutines for background tasks
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)


    // Testing libraries (you likely have these)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
