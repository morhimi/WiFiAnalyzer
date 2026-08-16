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

val versionProperties =
    Properties().apply {
        val propFile = file("build.properties")
        if (propFile.canRead()) {
            propFile.inputStream().use { load(it) }
        }
    }
val versionMajor = versionProperties.getProperty("version_major", "3")
val versionMinor = versionProperties.getProperty("version_minor", "3")
val versionPatch = versionProperties.getProperty("version_patch", "1")
val versionStore = versionProperties.getProperty("version_store", "72").toInt()
val versionBuild = versionProperties.getProperty("version_build", "0")

configure<ApplicationExtension> {
    namespace = "com.vrem.wifianalyzer"
    compileSdk = 37
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.vrem.wifianalyzer"
        minSdk = 24
        targetSdk = 37
        versionCode = versionStore
        versionName = "$versionMajor.$versionMinor.$versionPatch.$versionBuild"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        val keystorePropertiesFile = file("androidkeystore.properties")
        if (keystorePropertiesFile.canRead()) {
            val properties =
                Properties().apply {
                    keystorePropertiesFile.inputStream().use { load(it) }
                }
            create("release") {
                keyAlias = properties.getProperty("key_alias")
                keyPassword = properties.getProperty("key_password")
                storeFile = file(properties.getProperty("store_filename"))
                storePassword = properties.getProperty("store_password")
            }
        }
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
            signingConfigs.findByName("release")?.let {
                signingConfig = it
            }
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    lint {
        lintConfig = file("lint.xml")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
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
        "**/Hilt_*.*",
        "**/Dagger*.*",
        "**/*_HiltComponents*.*",
        "**/*_GeneratedInjector*.*",
        "**/*_MembersInjector*.*",
        "**/*_Factory*.*",
    )

val classKotlinDir = layout.buildDirectory.dir("intermediates/classes/debug/transformDebugClassesWithAsm/dirs")
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

// versioning tasks ----------------------------------------
tasks.register("bumpReleaseVersion") {
    description = "Increments patch and store versions in build.properties for release"
    group = "versioning"
    doLast {
        val propFile = file("build.properties")
        val props =
            Properties().apply {
                if (propFile.canRead()) {
                    propFile.inputStream().use { load(it) }
                }
            }
        val newPatch = props.getProperty("version_patch", "0").toInt() + 1
        val newStore = props.getProperty("version_store", "0").toInt() + 1
        props.setProperty("version_patch", newPatch.toString())
        props.setProperty("version_store", newStore.toString())
        props.setProperty("version_build", "0")
        propFile.writer().use { props.store(it, "Build Properties") }
        println(
            ">>> Updated release version: ${props["version_major"]}.${props["version_minor"]}.$newPatch ($newStore)",
        )
    }
}

tasks.register("bumpBuildVersion") {
    description = "Increments build version in build.properties"
    group = "versioning"
    doLast {
        val propFile = file("build.properties")
        val props =
            Properties().apply {
                if (propFile.canRead()) {
                    propFile.inputStream().use { load(it) }
                }
            }
        val newBuild = props.getProperty("version_build", "0").toInt() + 1
        props.setProperty("version_build", newBuild.toString())
        propFile.writer().use { props.store(it, "Build Properties") }
        println(">>> Updated build version to: $newBuild")
    }
}

configurations.configureEach {
    exclude(group = "org.hamcrest", module = "hamcrest-core")
    exclude(group = "org.hamcrest", module = "hamcrest-library")
}
