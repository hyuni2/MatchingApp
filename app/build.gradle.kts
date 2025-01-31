plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.matchingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.matchingapp"
        minSdk = 24
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation("com.google.android.material:material:1.4.0")
        //chip component(필터링) 위해 추가한 dependancy -> 오류시 수정
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
        //mypage 프로필 이미지 유지 위해 Glide 라이브러리 추가
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}

/*// 네이버 지도 api
dependencies {
    implementation ("com.naver.maps:map-sdk:3.16.2")  // 네이버 지도 API
    implementation ("com.squareup.okhttp3:okhttp:4.9.3") // Reverse Geocoding API 호출용
}*/
// 네이버 지도 api
//dependencies {
    //implementation ("com.naver.maps:map-sdk:3.16.2")  // 네이버 지도 API
    //implementation ("com.squareup.okhttp3:okhttp:4.9.3") // Reverse Geocoding API 호출용
//}