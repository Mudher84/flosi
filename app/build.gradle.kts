plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val flosiFirebaseApiKey = providers.gradleProperty("FLOSI_FIREBASE_API_KEY")
    .orElse("AIzaSyDxUooW3VAkHpzt7Kj_6SygQ9mHxYloY04")
    .get()
val flosiFirebaseAppId = providers.gradleProperty("FLOSI_FIREBASE_APP_ID")
    .orElse("1:897529405735:android:0adef25c67db7d03e88902")
    .get()
val flosiFirebaseProjectId = providers.gradleProperty("FLOSI_FIREBASE_PROJECT_ID")
    .orElse("flosi-7133e")
    .get()
val flosiGoogleWebClientId = providers.gradleProperty("FLOSI_GOOGLE_WEB_CLIENT_ID")
    .orElse("897529405735-h0ijqqgoemls5hje4ucrfckcoq49fo27.apps.googleusercontent.com")
    .get()

android {
    namespace = "com.flosi.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.flosi.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "1.4.0-premium-rtl-home"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FLOSI_FIREBASE_API_KEY", flosiFirebaseApiKey.asBuildConfigString())
        buildConfigField("String", "FLOSI_FIREBASE_APP_ID", flosiFirebaseAppId.asBuildConfigString())
        buildConfigField("String", "FLOSI_FIREBASE_PROJECT_ID", flosiFirebaseProjectId.asBuildConfigString())
        buildConfigField("String", "FLOSI_GOOGLE_WEB_CLIENT_ID", flosiGoogleWebClientId.asBuildConfigString())
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.08.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.9.3")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")

    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.work:work-runtime-ktx:2.10.2")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.core:core-ktx:1.16.0")

    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
