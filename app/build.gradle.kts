plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.dagger)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinParcelize)
    kotlin("kapt")
}

android {
    namespace = "dev.kingtux.batterymonitor.phone"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.kingtux.batterymonitor"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    buildFeatures {
        compose = true
        viewBinding = true
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
    wearApp(project(mapOf("path" to ":wear")))
    implementation(project(mapOf("path" to ":common")))

    implementation(libs.kotlinSerializeJson)

    implementation(libs.androidXLifecycle)
    implementation(libs.androidXActivity)
    implementation(platform(libs.composeBom))
    implementation(libs.androidXComposeUI)
    implementation(libs.androidXComposeUITooling)
    implementation(libs.androidXComposeUIGraphics)
    implementation(libs.androidXComposeMaterial)
    implementation(libs.androidXAppCompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidXTestJunit)
    androidTestImplementation(libs.androidXTestEspresso)
    androidTestImplementation(platform(libs.composeBom))
    androidTestImplementation(libs.androidXUITestJunit)
    debugImplementation(libs.androidXComposeUITooling)
    debugImplementation(libs.androidXUITestManifest)
    implementation(libs.googlePlayServicesWearable)
    implementation(libs.androidXStartup)


    implementation(libs.hiltAndroid)
    kapt(libs.hiltAndroidCompiler)
}
kapt {
    correctErrorTypes = true
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}