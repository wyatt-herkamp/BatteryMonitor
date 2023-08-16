plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinParcelize)
    kotlin("kapt")
}

android {
    namespace = "dev.kingtux.batterymonitor"
    compileSdk = 33

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.kotlinSerializeJson)

    implementation(libs.androidXKotlin)
    implementation(libs.androidXAppCompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidXTestJunit)
    androidTestImplementation(libs.androidXTestEspresso)

    implementation(libs.googlePlayServicesWearable)

}
kapt {
    correctErrorTypes = true
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}