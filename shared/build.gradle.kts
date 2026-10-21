import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

abstract class GenerateConfigTask : DefaultTask() {
    @get:Input
    abstract val openRouterApiKey: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val key = openRouterApiKey.getOrElse("")
        val file = outputDir.get().file("com/cybersecurity/aianalyzer/config/AppConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.cybersecurity.aianalyzer.config

            object AppConfig {
                const val OPENROUTER_API_KEY: String = "$key"
            }
            """.trimIndent()
        )
    }
}

val openRouterApiKeyProvider = providers.provider {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }
    localProperties.getProperty("OPENROUTER_API_KEY")
        ?: System.getenv("OPENROUTER_API_KEY")
        ?: ""
}

val generateConfigTask = tasks.register<GenerateConfigTask>("generateConfig") {
    openRouterApiKey.set(openRouterApiKeyProvider)
    outputDir.set(layout.buildDirectory.dir("generated/source/config/commonMain/kotlin"))
}

kotlin {
    android {
        namespace = "com.cybersecurity.aianalyzer.shared"
        compileSdk = 37
        minSdk = 24

        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generateConfigTask)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(libs.ui.tooling.preview)
                implementation(libs.ui.tooling)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        androidMain.dependencies {
            implementation(libs.ui.tooling)
            implementation(libs.ktor.client.cio)
        }

        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}
