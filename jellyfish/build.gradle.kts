import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotest.multiplatform)
    `maven-publish`
}

group = "com.github.ProjectOpenSea"
version = "0.1.0"

kotlin {
    explicitApi = ExplicitApiMode.Strict

    jvm { compilations.all { kotlinOptions { jvmTarget = "11" } } }

    ios()
    // Add the ARM64 simulator target in order to run iosSimulatorArm64Test on M1 machines.
    iosSimulatorArm64()

    val iosMain by sourceSets.getting
    val iosTest by sourceSets.getting
    val iosSimulatorArm64Main by sourceSets.getting
    val iosSimulatorArm64Test by sourceSets.getting

    iosSimulatorArm64Main.dependsOn(iosMain)
    iosSimulatorArm64Test.dependsOn(iosTest)

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(libs.kotlinxCoroutinesCore)
            }
        }
        val commonTest by getting {
             dependencies {
                 implementation(libs.moleculeRuntime)
                 implementation(libs.turbine)
                 implementation(libs.kotest.assertions.core)
                 implementation(libs.kotest.framework.engine)
                 implementation(libs.kotest.framework.datatest)
                 implementation(kotlin("test-common"))
                 implementation(kotlin("test-annotations-common"))
             }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }

    tasks.named<Test>("jvmTest") {
        useJUnitPlatform()
        filter {
            isFailOnNoMatchingTests = false
        }
        testLogging {
            showExceptions = true
            showStandardStreams = true
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
            )
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}