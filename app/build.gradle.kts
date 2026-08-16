/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.allopen)
    id("jacoco")
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// dependencies -------------------------------------------------
dependencies {
    // Compile Build Dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.collection.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.media)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.google.material)
    implementation(libs.vico.views)
    implementation(libs.kotlin.stdlib)
    // Hilt Dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // Compose Dependencies
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.activity)
    implementation(libs.compose.lifecycle.runtime)
    implementation(libs.compose.lifecycle.viewmodel)
    implementation(libs.compose.savedstate.ktx)
    implementation(libs.compose.navigation)
    implementation(libs.compose.hilt.navigation)
    implementation(libs.compose.datastore.preferences)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    // Unit Test Dependencies
    testImplementation(libs.test.androidx.ext.junit)
    testImplementation(libs.test.junit.toolbox)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.assertj.core)
    testImplementation(libs.test.hamcrest)
    testImplementation(libs.test.kotlin)
    testImplementation(libs.test.kotlin.junit)
    testImplementation(libs.test.kotlinx.coroutines)
    testImplementation(libs.test.mockito.core)
    testImplementation(libs.test.mockito.kotlin)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.test.slf4j.simple)
    // Android Test Dependencies
    androidTestImplementation(libs.androidTest.espresso.contrib)
    androidTestImplementation(libs.androidTest.espresso.core)
    androidTestImplementation(libs.androidTest.androidx.ext.junit.ktx)
    androidTestImplementation(libs.androidTest.androidx.rules)
    androidTestImplementation(libs.androidTest.assertj.core)
    androidTestImplementation(libs.androidTest.hamcrest)
}

configure<ApplicationExtension> {
    namespace = "com.vrem.wifianalyzer"
    compileSdk = 37
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.vrem.wifianalyzer"
        minSdk = 24
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
        debug {
            applicationIdSuffix = ".BETA"
            versionNameSuffix = "-BETA"
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-XXLanguage:+ExplicitBackingFields")
        }
    }

    lint {
        lintConfig = file("lint.xml")
    }
}

tasks.withType<Test>().configureEach {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    maxHeapSize = "2g"
    forkEvery = 200
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        outputs.upToDateWhen { false }
        showStandardStreams = true
    }
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

allOpen {
    annotation("com.vrem.annotation.OpenClass")
}

// jacoco ---------------------------------------------------
configure<JacocoPluginExtension> {
    toolVersion = libs.versions.jacoco.get()
}

val executionPath = "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"

val fileFilter =
    listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/databinding/*.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*\$DefaultImpls.class",
    )

val classKotlinDir = layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
val mainKotlinSrc = layout.projectDirectory.dir("src/main/kotlin")
val debugTree =
    fileTree(classKotlinDir) {
        exclude(fileFilter)
    }

val jacocoReportProvider =
    tasks.register<JacocoReport>("jacocoTestReport") {
        dependsOn("testDebugUnitTest")
        reports {
            csv.required.set(false)
            xml.required.set(true)
            html.required.set(true)
        }
        sourceDirectories.from(files(mainKotlinSrc))
        classDirectories.from(files(debugTree))
        executionData.from(
            fileTree(layout.buildDirectory) {
                include(executionPath)
            },
        )
    }

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(jacocoReportProvider)
    sourceDirectories.from(files(mainKotlinSrc))
    classDirectories.from(files(debugTree))
    executionData.from(
        fileTree(layout.buildDirectory) {
            include(executionPath)
        },
    )
    violationRules {
        isFailOnViolation = true
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                minimum = "0.98".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.95".toBigDecimal()
            }
            limit {
                counter = "COMPLEXITY"
                minimum = "0.96".toBigDecimal()
            }
            limit {
                counter = "LINE"
                minimum = "0.99".toBigDecimal()
            }
            limit {
                counter = "METHOD"
                minimum = "0.98".toBigDecimal()
            }
            limit {
                counter = "CLASS"
                minimum = "0.99".toBigDecimal()
            }
        }
    }
}

// keystore and version helper -----------------------------
object BuildHelper {
    fun isTestTask(gradle: org.gradle.api.invocation.Gradle): Boolean {
        val taskNames = gradle.startParameter.taskNames
        return ":app:testDebugUnitTest" in taskNames ||
            "testDebugUnitTest" in taskNames ||
            ":app:testReleaseUnitTest" in taskNames ||
            "testReleaseUnitTest" in taskNames
    }

    fun isReleaseTask(gradle: org.gradle.api.invocation.Gradle): Boolean {
        val taskNames = gradle.startParameter.taskNames
        return ":app:assembleRelease" in taskNames ||
            "assembleRelease" in taskNames ||
            ":app:bundleRelease" in taskNames ||
            "bundleRelease" in taskNames
    }

    fun readProperties(propertiesFile: File): Properties {
        if (propertiesFile.canRead()) {
            val properties = Properties()
            propertiesFile.inputStream().use { inputStream ->
                properties.load(inputStream)
            }
            return properties
        } else {
            val message = ">>> Could not read ${propertiesFile.name} file!"
            System.err.println(message)
            throw RuntimeException(message)
        }
    }

    fun writeProperties(
        propertiesFile: File,
        properties: Properties,
    ) {
        propertiesFile.writer().use { writer ->
            properties.store(writer, "Build Properties")
        }
    }

    fun configureSigning(project: Project) {
        if (isReleaseTask(project.gradle)) {
            val propertiesFile = project.file("androidkeystore.properties")
            if (propertiesFile.exists()) {
                val properties = readProperties(propertiesFile)
                println(">>> Signing Config $properties")
                val android = project.extensions.getByType<ApplicationExtension>()
                val releaseConfig =
                    android.signingConfigs.create("releaseConfig") {
                        keyAlias = properties["key_alias"].toString()
                        keyPassword = properties["key_password"].toString()
                        storeFile = project.file(properties["store_filename"].toString())
                        storePassword = properties["store_password"].toString()
                    }
                android.buildTypes.getByName("release").signingConfig = releaseConfig
            } else {
                System.err.println(">>> No Signing Config found! Missing '${propertiesFile.name}' file!")
            }
        }
    }

    fun updateVersion(project: Project) {
        val propertiesFile = project.file("build.properties")
        val properties = readProperties(propertiesFile)

        var versionMajor = properties["version_major"].toString().toInt()
        var versionMinor = properties["version_minor"].toString().toInt()
        var versionPatch = properties["version_patch"].toString().toInt()
        var versionBuild = properties["version_build"].toString().toInt()
        var versionStore = properties["version_store"].toString().toInt()

        if (isReleaseTask(project.gradle)) {
            println(">>> Building Release...")
            versionPatch++
            versionStore++
            versionBuild = 0
            properties["version_patch"] = versionPatch.toString()
            properties["version_store"] = versionStore.toString()
            properties["version_build"] = versionBuild.toString()
            writeProperties(propertiesFile, properties)
        }
        if (isTestTask(project.gradle)) {
            println(">>> Running Tests...")
            versionBuild++
            properties["version_build"] = versionBuild.toString()
            writeProperties(propertiesFile, properties)
        }

        val android = project.extensions.getByType<ApplicationExtension>()
        var versionName = "$versionMajor.$versionMinor.$versionPatch"
        var applicationId = android.defaultConfig.applicationId ?: ""
        if (!isReleaseTask(project.gradle)) {
            versionName = "$versionName.$versionBuild"
            applicationId += (android.buildTypes.getByName("debug").applicationIdSuffix ?: "")
        }
        println(">>> ${project.parent?.name} $versionName ($versionStore) $applicationId")
        android.defaultConfig.versionCode = versionStore
        android.defaultConfig.versionName = versionName
    }
}

BuildHelper.configureSigning(project)
BuildHelper.updateVersion(project)

configurations.configureEach {
    exclude(group = "org.hamcrest", module = "hamcrest-core")
    exclude(group = "org.hamcrest", module = "hamcrest-library")
}
