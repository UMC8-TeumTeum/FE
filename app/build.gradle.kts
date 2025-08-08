import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
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
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }
}

dependencies {

    // 친구 인디케이터
    implementation ("com.tbuonomo:dotsindicator:4.3")

    // 달력
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.5")

    // 프로필
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    // profileImageUrl
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    kapt ("com.github.bumptech.glide:compiler:4.15.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //bottom nav
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.android.material:material:1.11.0")

    //grid
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    //Material Component
    implementation("com.google.android.material:material:1.12.0")

    //roomdb
    implementation ("androidx.room:room-ktx:2.5.2")
    implementation ("androidx.room:room-runtime:2.5.2")
    kapt ("androidx.room:room-compiler:2.5.2")

    //lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

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

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-android-compiler:2.51")

    //kakao
    implementation("com.kakao.sdk:v2-user:2.19.0")
}