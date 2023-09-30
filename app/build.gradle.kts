val compose_version: String by rootProject.extra
val work_version: String by rootProject.extra
val lifecycle_version: String by rootProject.extra
val nav_version: String by rootProject.extra
val activity_version: String by rootProject.extra
val mat3_version: String by rootProject.extra
val glance_version: String by rootProject.extra
val datastore_version: String by rootProject.extra
val immutable_collections_version: String by rootProject.extra
val serialization_version: String by rootProject.extra
val junit_version: String by rootProject.extra
val androidx_junit_version: String by rootProject.extra
val espresso_version: String by rootProject.extra

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "1.8.21"
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
//        proguardFiles("proguard-rules.pro",
//            /*getDefaultProguardFile(*/"proguard-android-optimize.txt"/*)*/)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles += listOf(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                File("proguard-rules.pro")
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.activity:activity-compose:$activity_version")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.compose.material3:material3:$mat3_version")
    implementation("androidx.work:work-runtime-ktx:$work_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // For Glance support
    implementation ("androidx.glance:glance:$glance_version")
    // For AppWidgets support
    implementation ("androidx.glance:glance-appwidget:$glance_version")

    implementation("androidx.datastore:datastore:$datastore_version")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:$immutable_collections_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

    testImplementation("junit:junit:$junit_version")
    androidTestImplementation("androidx.test.ext:junit:$androidx_junit_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
}
