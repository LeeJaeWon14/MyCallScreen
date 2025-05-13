import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
}

android {
    namespace = "com.jeepchief.mycallscreen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jeepchief.mycallscreen"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../jeepchief_keystore.jks")
            storePassword = project.findProperty("SIGNED_STORE_PASSWORD") as String
            keyAlias = project.findProperty("SIGNED_KEY_ALIAS") as String
            keyPassword = project.findProperty("SIGNED_KEY_PASSWORD") as String
        }
        create("release") {
            storeFile = file("../jeepchief_keystore.jks")
            storePassword = project.findProperty("SIGNED_STORE_PASSWORD") as String
            keyAlias = project.findProperty("SIGNED_KEY_ALIAS") as String
            keyPassword = project.findProperty("SIGNED_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.get("release")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.get("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl

            val appName = rootProject.name
//            val formattedDate = java.text.SimpleDateFormat("yyyyMMdd").format(java.util.Date())
            val formattedDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date(System.currentTimeMillis()))
            val verName = "v${versionName}_(${versionCode})_$formattedDate"

            val originalName = outputImpl.outputFileName
            var newName = originalName
                .replace("app-", "${appName}-")
                .replace("-release", "_release_$verName")
                .replace("-debug", "_debug_$verName")

            outputImpl.outputFileName = newName
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)
    implementation(libs.google.accompanist)
    implementation(libs.androidx.room)
    implementation(libs.room.ktx)
//    implementation(libs.room.compiler)
    kapt(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}