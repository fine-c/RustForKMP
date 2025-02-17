plugins {
    kotlin("multiplatform")
    id("io.gitlab.trixnity.cargo.kotlin.multiplatform")
    id("io.gitlab.trixnity.uniffi.kotlin.multiplatform")
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.android.library)
}

uniffi {
    bindgenFromPath(rootProject.layout.projectDirectory.dir("bindgen"))
    generateFromLibrary()
}

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

android {
    namespace = "com.fine.samples.arithmeticpm"
    compileSdk = 34

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        ndk.abiFilters += listOf("x86", "armeabi-v7a", "x86_64", "arm64-v8a")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
