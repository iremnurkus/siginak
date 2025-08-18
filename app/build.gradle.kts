import org.jetbrains.kotlin.fir.declarations.builder.buildScript

plugins {
    id ("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // BUNU EKLE
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.siginak"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.siginak"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-runtime-android:2.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.04.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("org.jsoup:jsoup:1.15.4") // Jsoup ile HTML'den veri çekmek için
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation("com.google.maps.android:maps-compose")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("com.google.android.gms:play-services-maps:19.2.0") // (veya günceli)
    implementation ("com.google.firebase:firebase-messaging:24.1.1")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    implementation ("androidx.compose.material3:material3:1.1.0")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.7.0")
    implementation ("androidx.navigation:navigation-compose:2.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.10.1")
    // NotificationCompat
    implementation ("androidx.core:core-ktx:1.16.0")
    implementation ("com.google.android.libraries.places:places:4.3.1")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    // Location & Maps
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.4")
    // Places SDK
    implementation ("androidx.work:work-runtime-ktx:2.8.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.10.0")
    implementation ("androidx.core:core-ktx:1.9.0")
    implementation ("androidx.core:core:1.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("org.json:json:20231013")
    // Compose ve Maps
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.maps.android:maps-compose:2.11.4")
    // HTTP istemcisi (Retrofit + Moshi örneği)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    // Coroutine desteği
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // Retrofit + Moshi
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.moshi:moshi-kotlin:1.14.0")

    // WorkManager
    implementation ("androidx.work:work-runtime-ktx:2.8.1")

    // Jetpack Compose
    implementation ("androidx.compose.ui:ui:1.4.0")
    implementation ("androidx.compose.material:material:1.4.0")
    implementation ("androidx.activity:activity-compose:1.7.0")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.30.1")

    // Coroutines & Lifecycle
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("com.google.firebase:firebase-appcheck-debug:16.0.0")

    // Gerçek cihazlarda Play Integrity kullanmak için
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.1")
}