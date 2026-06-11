plugins {
    id("com.android.application") version "8.13.2"
    id("org.jetbrains.kotlin.android") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

android {
    namespace = "hai.qstory.plugin.manager"
    compileSdk = 37

    defaultConfig {
        applicationId = "hai.qstory.plugin.manager"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release-key.jks")
            storePassword = "qstory2024"
            keyAlias = "qstory"
            keyPassword = "qstory2024"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerVersion = "2.1.0"
    }

    // 禁用 AAB，只生成 APK
    dependenciesInfo.includeInApk = false

    // 禁用 bundle 工具生成 apks
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xskip-metadata-version-check"
        )
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))

    // MIUIx
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:0.9.2")
    implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.2")
    implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.2")
    implementation("top.yukonga.miuix.kmp:miuix-navigation3-ui:0.9.2")

    implementation("androidx.navigation3:navigation3-runtime:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.foundation:foundation")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
}
