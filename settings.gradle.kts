// ПОСЛЕДНЯЯ ПОПЫТКА: ИСПОЛЬЗУЕМ maven.google.com

pluginManagement {
    repositories {
        // Заменяем aliyun на maven.google.com
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // И здесь тоже заменяем
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
    }
}

rootProject.name = "Napominalka"
include(":app")
