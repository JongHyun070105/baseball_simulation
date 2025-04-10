plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.baseball_simulation_app"
    compileSdk = 35

    buildFeatures{
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.baseball_simulation_app"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ViewModel과 LiveData 관련 라이브러리
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v261)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Fragment KTX 추가 (activityViewModels() 사용을 위해 필수)
    implementation(libs.androidx.fragment.ktx)

    // Retrofit 및 네트워크 관련
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)

    // 이미지 로딩
    annotationProcessor(libs.compiler)

    // MPAndroidChart 라이브러리 - 중복된 구현 제거
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // 코루틴
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx.v251)

    // 기존 의존성들
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity)

    implementation(libs.circleimageview)

    implementation(libs.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)

    implementation(libs.androidsvg)

    // Glide - 중복된 구현 제거
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Compose 관련 의존성
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}