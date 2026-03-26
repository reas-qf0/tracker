import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.hotReload)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.build.config)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.activity)
            implementation(libs.androidx.work.runtime)
            implementation(libs.koin.android)
            //testImplementation(libs.junit)
            //androidTestImplementation(libs.androidx.espresso.core)
            //androidTestImplementation(project.dependencies.platform(libs.androidx.compose.bom))
            //androidTestImplementation(libs.androidx.compose.ui.test.junit4)
            //debugImplementation(libs.androidx.compose.ui.tooling)
            //debugImplementation(libs.androidx.compose.ui.test.manifest)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.graphics)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.androidx.datastore.core)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.androidx.paging.compose)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.landscapist.image)
            implementation(libs.landscapist.palette)
            implementation(libs.landscapist.animation)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.serialization.json)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.logging)
            implementation(libs.colormath)
            implementation(libs.colormath.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.koin.logger.slf4j)
            implementation(libs.logback)
        }
    }
}

android {
    namespace = "com.reas.tracker2"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.reas.tracker2"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("release-signing.properties")
            val keystoreProperties = Properties()
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))

            storeFile = rootProject.file(keystoreProperties["storeFile"].toString())
            storePassword = keystoreProperties["storePassword"].toString()
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
        }
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

compose.desktop {
    application {
        mainClass = "com.reas.tracker2.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.reas.tracker2"
            packageVersion = "1.0.0"
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName("com.reas.tracker2.platform")

    buildConfigField("IS_DEBUG", true)

    buildConfigField("IS_ANDROID", expect(false))
    buildConfigField("IS_DESKTOP", expect(false))
    buildConfigField("IS_WINDOWS", expect(false))
    buildConfigField("IS_LINUX", expect(false))
    sourceSets.named("androidMain") {
        buildConfigField("IS_ANDROID", true)
    }
    sourceSets.named("jvmMain") {
        buildConfigField("IS_DESKTOP", true)
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            buildConfigField("IS_WINDOWS", true)
        } else {
            buildConfigField("IS_LINUX", true)
        }
    }
}