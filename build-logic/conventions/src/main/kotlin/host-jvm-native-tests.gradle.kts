import org.gradle.accessors.dm.*
import io.gitlab.trixnity.gradle.rust.dsl.hostNativeTarget

plugins {
    id("io.gitlab.trixnity.rust.kotlin.multiplatform")
    kotlin("multiplatform")
    kotlin("plugin.atomicfu")
}

kotlin {
    jvmToolchain(17)
    jvm()
    hostNativeTarget()
    sourceSets {
        // https://github.com/gradle/gradle/issues/15383
        val libs = the<LibrariesForLibs>()

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
    }
}
