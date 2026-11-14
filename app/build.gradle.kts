plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.fluidnotify"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.fluidnotify"
        minSdk = 31
        targetSdk = 34
        versionCode = 27
        versionName = "0.3.20"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    packaging {
        resources.excludes += setOf("META-INF/*.kotlin_module")
    }
}

dependencies {
    // Xposed API 只在编译期需要，运行时由框架提供
    compileOnly("de.robv.android.xposed:api:82")
}
