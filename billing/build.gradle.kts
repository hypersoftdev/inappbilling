plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = (findProperty("group") as String?) ?: "com.hypersoft.billing"
version = (findProperty("version") as String?) ?: "4.0.0"

android {
    namespace = "com.hypersoft.billing"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
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
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = project.group.toString()
                artifactId = rootProject.name
                version = project.version.toString()
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Google Play Billing
    implementation(libs.billing.ktx)

    // Coroutines (Dispatchers.Main requires the Android dispatcher implementation)
    implementation(libs.kotlinx.coroutines.android)
}