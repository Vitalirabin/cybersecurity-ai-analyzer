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
    @get:Input
    abstract val cloudBaseUrl: Property<String>
    @get:Input
    abstract val cloudModel: Property<String>
    @get:Input
    abstract val ollamaBaseUrl: Property<String>
    @get:Input
    abstract val ollamaModel: Property<String>
    @get:Input
    abstract val smallLogThreshold: Property<Int>
    @get:Input
    abstract val fallbackModelsStr: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val key = openRouterApiKey.getOrElse("")
        val cloudUrl = cloudBaseUrl.getOrElse("")
        val cloudMod = cloudModel.getOrElse("")
        val ollamaUrl = ollamaBaseUrl.getOrElse("")
        val ollamaMod = ollamaModel.getOrElse("")
        val threshold = smallLogThreshold.getOrElse(500)
        val fallbacks = fallbackModelsStr.getOrElse("")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(separator = "\", \"") { it }

        val file = outputDir.get().file("com/cybersecurity/aianalyzer/config/AppConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.cybersecurity.aianalyzer.config

            object AppConfig {
                const val OPENROUTER_API_KEY: String = "$key"
                const val CLOUD_BASE_URL: String = "$cloudUrl"
                const val CLOUD_MODEL: String = "$cloudMod"
                const val OLLAMA_BASE_URL: String = "$ollamaUrl"
                const val OLLAMA_MODEL: String = "$ollamaMod"
                const val SMALL_LOG_THRESHOLD: Int = $threshold
                val FALLBACK_MODELS: List<String> = listOf("$fallbacks")
            }
            """.trimIndent()
        )
    }
}

val localPropertiesProvider = providers.provider {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { localProperties.load(it) }
    }
    localProperties
}

abstract class GenerateConfigTestTask : GenerateConfigTask()

val generateConfigTaskTyped = tasks.register<GenerateConfigTask>("generateConfigCustom") {
    openRouterApiKey.set(localPropertiesProvider.map { it.getProperty("OPENROUTER_API_KEY") ?: System.getenv("OPENROUTER_API_KEY") ?: "" })
    cloudBaseUrl.set(localPropertiesProvider.map { it.getProperty("CLOUD_BASE_URL") ?: System.getenv("CLOUD_BASE_URL") ?: "" })
    cloudModel.set(localPropertiesProvider.map { it.getProperty("CLOUD_MODEL") ?: System.getenv("CLOUD_MODEL") ?: "" })
    ollamaBaseUrl.set(localPropertiesProvider.map { it.getProperty("OLLAMA_BASE_URL") ?: System.getenv("OLLAMA_BASE_URL") ?: "" })
    ollamaModel.set(localPropertiesProvider.map { it.getProperty("OLLAMA_MODEL") ?: System.getenv("OLLAMA_MODEL") ?: "" })
    smallLogThreshold.set(localPropertiesProvider.map { it.getProperty("SMALL_LOG_THRESHOLD")?.toIntOrNull() ?: System.getenv("SMALL_LOG_THRESHOLD")?.toIntOrNull() ?: 500 })
    fallbackModelsStr.set(localPropertiesProvider.map { it.getProperty("FALLBACK_MODELS") ?: System.getenv("FALLBACK_MODELS") ?: "" })

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
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateConfigTaskTyped)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.coroutines.core)

                api(libs.koin.core)
                api(libs.koin.compose)
                api(libs.koin.compose.viewmodel)
            }
        }

        androidMain.dependencies {
            implementation(libs.ui.tooling)
            implementation(libs.ktor.client.cio)
            implementation(libs.koin.android.sdk)
        }

        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.cio)
            implementation(libs.kotlinx.coroutines.swing)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}
