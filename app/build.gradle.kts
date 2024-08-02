plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.testrun"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testrun"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.circleimageview)
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)


    //Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    //RoundImage
    implementation ("com.makeramen:roundedimageview:2.3.0")
    // media library
    implementation("androidx.media:media:1.7.0")
}