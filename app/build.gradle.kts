plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.hypersoft.inappbilling"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hypersoft.inappbilling"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Billing Library
    implementation(project(":billing"))
    //implementation("com.github.hypersoftdev:inappbilling:4.0.0")
}