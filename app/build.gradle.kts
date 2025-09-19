import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.example.teumteum"
    compileSdk = 35

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.teumteum"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TEMP_ACCESS_TOKEN", "\"${properties["TEMP_ACCESS_TOKEN"]}\"")
        manifestPlaceholders["TEMP_ACCESS_TOKEN"] = properties["TEMP_ACCESS_TOKEN"] ?: ""

        buildConfigField("String", "BASE_URL", "\"${properties["BASE_URL"]}\"")
        manifestPlaceholders["BASE_URL"] = properties["BASE_URL"] ?: ""

        buildConfigField("String", "NATIVE_APP_KEY", "\"${properties["NATIVE_APP_KEY"]}\"")
        manifestPlaceholders["NATIVE_APP_KEY"] = properties["NATIVE_APP_KEY"] ?: ""
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xemit-jvm-type-annotations")
    }
}

dependencies {

    // 친구 인디케이터
    implementation ("com.tbuonomo:dotsindicator:4.3")

    // 프로필
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // glide
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    ksp("com.github.bumptech.glide:ksp:4.15.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //bottom nav
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    //grid
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    //Material Component
    implementation("com.google.android.material:material:1.12.0")

    //lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    //coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")

    //okHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    //kakao
    implementation("com.kakao.sdk:v2-user:2.19.0")

    //shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    // EncryptedSharedPreferences
    implementation ("androidx.security:security-crypto:1.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-messaging")

    // calendarview
    implementation("com.kizitonwose.calendar:view:2.7.0")
}