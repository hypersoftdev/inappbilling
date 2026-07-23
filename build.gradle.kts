// Top-level build file where you can add configuration options common to all sub-projects/modules.

// AGP 9's built-in Kotlin support pulls in KGP 2.2.10 by default; pin the newer stable version here instead
// of applying the (now-unsupported-by-AGP) org.jetbrains.kotlin.android plugin in each module.
buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}