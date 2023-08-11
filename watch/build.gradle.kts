plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}
val tiles by extra("1.2.0")
val protolayout by extra("1.0.0")
val HOROLOGIST_VERSION by extra("0.5.3")

android {
    namespace = "dev.kingtux.batterymonitor.watch"
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
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.wear.compose:compose-material:1.2.0")
    implementation("androidx.wear.compose:compose-foundation:1.2.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")

     implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.1.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.android.gms:play-services-wearable:18.0.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(project(mapOf("path" to ":common")))

    implementation("com.google.android.horologist:horologist-compose-layout:${HOROLOGIST_VERSION}")
    implementation("com.google.android.horologist:horologist-tiles:${HOROLOGIST_VERSION}")
    implementation("com.google.android.horologist:horologist-compose-tools:${HOROLOGIST_VERSION}")


    implementation("javax.inject:javax.inject:1")
    implementation("com.google.dagger:hilt-android:${rootProject.extra["HILT_VERISON"]}")
    kapt("com.google.dagger:hilt-android-compiler:${rootProject.extra["HILT_VERISON"]}")

    implementation("com.google.guava:guava:31.0.1-android")
    implementation("androidx.startup:startup-runtime:1.1.1")

    implementation("androidx.wear.tiles:tiles:$tiles")

    implementation("androidx.wear.protolayout:protolayout-material:$protolayout")
    implementation("androidx.wear.protolayout:protolayout:$protolayout")
    implementation("androidx.wear.protolayout:protolayout-expression:$protolayout")
}
kapt {
    correctErrorTypes = true
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}