// ПРАВИЛЬНЫЙ И ИСПРАВЛЕННЫЙ КОД ДЛЯ ФАЙЛА app/build.gradle.kts

plugins {
    id("com.android.application") version "8.5.1" // Сразу ставим стабильную версию
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" // Добавьте этот плагин, он нужен для Room
}

android {
    namespace = "com.example.napominalka" // <-- УКАЖИТЕ ВАШ ПАКЕТ
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.napominalka" // <-- УКАЖИТЕ ВАШ ПАКЕТ
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Версия для Kotlin 1.9.22
    }

    // ----- НАЧАЛО ИЗМЕНЕНИЯ -----
    // Этот блок исправляет ошибку 'Failed to parse XML file overlay_view.xml'
    packagingOptions {
        resources.excludes.add("**/overlay_view.xml")
    }
    // ----- КОНЕЦ ИЗМЕНЕНИЯ -----
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.05.00")) // Понижаем версию BOM для стабильности
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
    ksp("androidx.room:room-compiler:2.6.1") // <-- Важно для Room
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
