import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.google.firebase.crashlytics)
}

val localProperties = rootProject.file("local.properties").inputStream().use { input ->
    Properties().apply { load(input) }
}
val apiKey: String = localProperties.getProperty("API_KEY") ?: "\"\""
val admobAppId: String = localProperties.getProperty("ADMOB_APP_ID") ?: "SAMPLE_APP_ID"

val mainAdUnitId: String = localProperties.getProperty("MAIN_ADMOB_UNIT_ID") ?: "\"\""
val organizersAdUnitId: String = localProperties.getProperty("ORGANIZERS_ADMOB_UNIT_ID") ?: "\"\""
val notesAdUnitId: String = localProperties.getProperty("NOTES_ADMOB_UNIT_ID") ?: "\"\""
val syllabusAdUnitId: String = localProperties.getProperty("SYLLABUS_ADMOB_UNIT_ID") ?: "\"\""
val videosAdUnitId: String = localProperties.getProperty("VIDEOS_ADMOB_UNIT_ID") ?: "\"\""
val booksAdUnitId: String = localProperties.getProperty("BOOKS_ADMOB_UNIT_ID") ?: "\"\""

android {
    namespace = "com.subhajitrajak.makautstudybuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.subhajitrajak.makautstudybuddy"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "2.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        manifestPlaceholders["admob_app_id"] = admobAppId

        buildConfigField("String", "MAIN_ADMOB_UNIT_ID", "\"$mainAdUnitId\"")
        buildConfigField("String", "ORGANIZERS_ADMOB_UNIT_ID", "\"$organizersAdUnitId\"")
        buildConfigField("String", "NOTES_ADMOB_UNIT_ID", "\"$notesAdUnitId\"")
        buildConfigField("String", "SYLLABUS_ADMOB_UNIT_ID", "\"$syllabusAdUnitId\"")
        buildConfigField("String", "VIDEOS_ADMOB_UNIT_ID", "\"$videosAdUnitId\"")
        buildConfigField("String", "BOOKS_ADMOB_UNIT_ID", "\"$booksAdUnitId\"")
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
    implementation(libs.firebase.crashlytics)
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
    implementation(libs.firebase.messaging)

    // in-app updates
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // circle-image view
    implementation (libs.circleimageview)

    // hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)

    // gms play service
    implementation (libs.play.services.auth)
    implementation(libs.play.services.ads)

    // glide (image loading)
    implementation (libs.glide)

    // retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)

    // swipe to refresh layout
    implementation(libs.androidx.swiperefreshlayout)

    // scalable sp and dp by intuit
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)
}