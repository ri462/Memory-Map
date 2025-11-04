plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.rina.memorymap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rina.memorymap"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {


    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.material)

    implementation("com.google.android.material:material:1.9.0")

    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation("com.google.android.libraries.places:places:3.4.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core:1.12.0")

    implementation ("com.squareup.okhttp3:okhttp:4.9.3")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)





}