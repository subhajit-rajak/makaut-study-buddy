import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

val localProperties = rootProject.file("local.properties").inputStream().use { input ->
    Properties().apply { load(input) }
}
val apiKey: String = localProperties.getProperty("API_KEY") ?: "\"\""

android {
    namespace = "com.subhajitrajak.makautstudybuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.subhajitrajak.makautstudybuddy"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
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

    buildFeatures {
        viewBinding=true
        buildConfig=true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.googleid)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // pdf viewer
    implementation (libs.android.pdf.viewer)

    // view models
    implementation (libs.androidx.lifecycle.viewmodel)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

    // kotlin coroutines
    implementation (libs.kotlinx.coroutines.android)

    // firebase
    implementation (platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // in-app updates
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // circle-image view
    implementation (libs.circleimageview)

    // hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)

    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("com.facebook.android:facebook-login:18.0.2")

    // glide (image loading)
    implementation (libs.glide)

    // retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
}