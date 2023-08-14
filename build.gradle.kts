
buildscript {
    val HILT_VERISON by extra(2.47)
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

    val kotlinVersion ="1.9.0"
    val androidGradle = "8.1.0"
    id("com.android.application") version androidGradle apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.android.library") version androidGradle apply false

    alias(libs.plugins.kotlinAndroid)  apply false
    alias(libs.plugins.kotlinSerialize)  apply false
    alias(libs.plugins.kotlinParcelize)  apply false
    alias(libs.plugins.kotlinKapt)  apply false
    }