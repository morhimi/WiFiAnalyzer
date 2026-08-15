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
    id("com.android.application")
    id("kotlin-allopen")
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val kotlinVersion: String by rootProject.extra

// dependencies -------------------------------------------------
dependencies {
    // Compile Build Dependencies
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.annotation:annotation:1.10.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.collection:collection-ktx:1.6.0")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.media:media:1.8.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("com.patrykandpatrick.vico:views:3.2.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    // Hilt Dependencies
    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")
    // Compose Dependencies
    val composeBom = "2026.08.00"
    implementation(platform("androidx.compose:compose-bom:$composeBom"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.savedstate:savedstate-ktx:1.5.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Unit Test Dependencies
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("com.googlecode.junit-toolbox:junit-toolbox:2.4")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("org.slf4j:slf4j-simple:2.0.18")
    // Android Test Dependencies
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
    androidTestImplementation("org.assertj:assertj-core:3.27.7")
    androidTestImplementation("org.hamcrest:hamcrest:3.0")
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
    toolVersion = "0.8.14"
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
