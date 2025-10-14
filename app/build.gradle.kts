// ПРАВИЛЬНЫЙ И ИСПРАВЛЕННЫЙ КОД ДЛЯ ФАЙЛА app/build.gradle.kts

plugins {
    id("com.android.application") version "8.5.1"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.example.napominalka"
    compileSdk = 34

    // ----- НАЧАЛО ИЗМЕНЕНИЯ 1: БЛОК ДЛЯ ПОДПИСИ -----
    // Этот блок будет использоваться только для release-сборки.
    // Он читает пароли и алиас из секретов GitHub.
    signingConfigs {
        create("release") {
            // Путь к файлу ключа, который мы создадим в GitHub Actions
            storeFile = file("napominalka-key.jks")
            // Читаем переменные, которые мы передадим из GitHub Secrets
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    // ----- КОНЕЦ ИЗМЕНЕНИЯ 1 -----

    defaultConfig {
        applicationId = "com.example.napominalka"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // ----- НАЧАЛО ИЗМЕНЕНИЯ 2: ПРИМЕНЕНИЕ ПОДПИСИ -----
            // Говорим Gradle использовать нашу конфигурацию для release-сборки
            signingConfig = signingConfigs.getByName("release")
            // ----- КОНЕЦ ИЗМЕНЕНИЯ 2 -----
        }
    }
    compileOptions {
        // ----- НАЧАЛО ИЗМЕНЕНИЯ 3: ОБНОВЛЕНИЕ ВЕРСИИ JAVA -----
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // ----- КОНЕЦ ИЗМЕНЕНИЯ 3 -----

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packagingOptions {
        resources.excludes.add("**/overlay_view.xml")
    }
}

dependencies {
    // ... ваши зависимости остаются без изменений
    implementation(platform("androidx.compose:compose-bom:2024.05.00")) 
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
