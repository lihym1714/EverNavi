plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.navernavi"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.navernavi"
        minSdk = 33
        //noinspection EditedTargetSdkVersion
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isTestCoverageEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "33.0.1"
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.1")
    implementation("androidx.navigation:navigation-ui:2.7.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.naver.maps:map-sdk:3.17.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")


}

configurations.all {
    resolutionStrategy {
        eachDependency {
            if ((requested.group == "org.jetbrains.kotlin") && (requested.name.startsWith("kotlin-stdlib"))) {
                useVersion("1.8.22")
            }
        }
    }
}