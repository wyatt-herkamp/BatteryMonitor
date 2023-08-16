plugins {

    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.dagger)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinParcelize)
    kotlin("kapt")
}
val tiles by extra("1.2.0")
val protolayout by extra("1.0.0")
val HOROLOGIST_VERSION by extra("0.5.3")

android {
    namespace = "dev.kingtux.batterymonitor.wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.kingtux.batterymonitor"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.annotations.ExperimentalHorologistApi"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.kotlinSerializeJson)

    implementation(libs.androidXKotlin)
    implementation(libs.androidXStartup)

    implementation(platform(libs.composeBom))
    implementation(libs.androidXComposeUI)
    implementation(libs.androidXComposeUITooling)
    implementation(libs.androidXWearComposeMaterial)
    implementation(libs.androidXWearComposeFoundation)

    implementation(libs.androidXLifecycle)
    implementation(libs.androidXActivity)

    implementation(libs.androidXWatchfaceComplication)
    androidTestImplementation(platform(libs.composeBom))
    androidTestImplementation(libs.androidXUITestJunit)
    debugImplementation(libs.androidXComposeUITooling)
    debugImplementation(libs.androidXUITestManifest)

    implementation(libs.googlePlayServicesWearable)

    implementation(project(mapOf("path" to ":common")))

    implementation(libs.horologistComposeLayout)
    implementation(libs.horologistTiles)
    implementation(libs.horologistComposeTools)


    implementation(libs.javaxInject)
    implementation(libs.hiltAndroid)
    kapt(libs.hiltAndroidCompiler)

    implementation(libs.guava)
    implementation(libs.androidXKotlin)


    implementation(libs.tiles)
    implementation(libs.protoLayout)
    implementation(libs.protoLayoutMaterial)
    implementation(libs.protoLayoutExpression)
}
kapt {
    correctErrorTypes = true
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}