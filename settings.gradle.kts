pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    versionCatalogs {
        create("libs") {
            // Compose compiler is highly coupled to Kotlin version
            // See https://developer.android.com/jetpack/androidx/releases/compose-kotlin#pre-release_kotlin_compatibility
            val kotlinVersion = "1.8.10"
            version("kotlin", kotlinVersion)
            version("androidPlugin", "7.4.2")
            version("composeCompiler", "1.4.4")

            // BEGIN These should be upgraded in unison
            version("okhttp", "4.10.0")
            val okioVersion = "3.2.0"
            version("okio", okioVersion)
            version("conscrypt", "2.5.2")
            // END Unison

            // Rest
            version("kodein", "7.5.0")
            version("coroutines", "1.6.4")
            version("rome", "1.7.1-f8")
            version("moshi", "1.12.0")
            version("threeten", "1.2.2")
            version("jsoup", "1.7.3")
            version("tagsoup", "1.2.1")
            version("readability4j", "1.0.5")
            version("retrofit", "2.9.0")
            version("qrgen", "2.6.0")
            version("androidxCore", "1.7.0")
            version("androidxTestcore", "1.5.0")
            version("workmanager", "2.7.0")
            version("appcompat", "1.3.0")
            version("material", "1.6.1")
            version("preference", "1.1.0")
            version("testRunner", "1.4.0")
            version("lifecycle", "2.3.1")
            version("room", "2.4.3")
            // Compose related below
            version("compose", "2023.04.00")
            val activityCompose = "1.7.0"
            version("activityCompose", activityCompose)
            version("paging", "3.0.0")
            version("pagingCompose", "1.0.0-alpha17")
            version("accompanist", "0.27.1")
            version("coil", "2.2.2")
            version("androidWindow", "1.0.0")
            // Formerly customtabs
            version("androidxBrowser", "1.5.0")
            // Tests
            version("junit", "4.13.2")
            version("espresso", "3.3.0")
            version("mockk", "1.13.3")
            version("mockito", "2.13.0")
            version("androidx-test-junit-ktx", "1.1.4")

            // Plugins
            plugin("android-application", "com.android.application").versionRef("androidPlugin")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-kapt", "org.jetbrains.kotlin.kapt").versionRef("kotlin")
            plugin("kotlin-parcelize", "org.jetbrains.kotlin.plugin.parcelize").versionRef("kotlin")

            // BOMS
            library("okhttp-bom", "com.squareup.okhttp3", "okhttp-bom").versionRef("okhttp")
            library("coil-bom", "io.coil-kt", "coil-bom").versionRef("coil")
            library("compose-bom", "androidx.compose", "compose-bom").versionRef("compose")

            // Libraries
            library("room", "androidx.room", "room-compiler").versionRef("room")
            library("room-ktx", "androidx.room", "room-ktx").versionRef("room")
            library("room-paging", "androidx.room", "room-paging").versionRef("room")

            library(
                "work-runtime-ktx",
                "androidx.work",
                "work-runtime-ktx"
            ).versionRef("workmanager")

            library("core-ktx", "androidx.core", "core-ktx").versionRef("androidxCore")
            library("androidx-appcompat", "androidx.appcompat", "appcompat").versionRef("appcompat")

            library("androidx-preference", "androidx.preference", "preference").versionRef("preference")

            // ViewModel
            library(
                "lifecycle-runtime-ktx",
                "androidx.lifecycle",
                "lifecycle-runtime-ktx"
            ).versionRef("lifecycle")
            library(
                "lifecycle-viewmodel-ktx",
                "androidx.lifecycle",
                "lifecycle-viewmodel-ktx"
            ).versionRef("lifecycle")
            library(
                "lifecycle-viewmodel-savedstate",
                "androidx.lifecycle",
                "lifecycle-viewmodel-savedstate"
            ).versionRef("lifecycle")
            library(
                "paging-runtime-ktx",
                "androidx.paging",
                "paging-runtime-ktx"
            ).versionRef("paging")

            // Compose
            // Overriding this to newer version than BOM because of predictive back
            library(
                "activity-compose",
                "androidx.activity",
                "activity-compose"
            ).version {
                require(activityCompose)
            }
            library("ui", "androidx.compose.ui", "ui").withoutVersion()
            library("foundation", "androidx.compose.foundation", "foundation").withoutVersion()
            library(
                "foundation-layout",
                "androidx.compose.foundation",
                "foundation-layout"
            ).withoutVersion()
            library("compose-material3", "androidx.compose.material3", "material3").withoutVersion()
            library("compose-material", "androidx.compose.material", "material").withoutVersion()
            library(
                "compose-material-icons-extended",
                "androidx.compose.material",
                "material-icons-extended"
            ).withoutVersion()
            library("runtime", "androidx.compose.runtime", "runtime").withoutVersion()
            library("ui-tooling", "androidx.compose.ui", "ui-tooling").withoutVersion()
            library(
                "navigation-compose",
                "androidx.navigation",
                "navigation-compose"
            ).withoutVersion()
            library(
                "paging-compose",
                "androidx.paging",
                "paging-compose"
            ).versionRef("pagingCompose")
            library("window", "androidx.window", "window").versionRef("androidWindow")
            library(
                "android-material",
                "com.google.android.material",
                "material"
            ).versionRef("material")
            library(
                "accompanist-permissions",
                "com.google.accompanist",
                "accompanist-permissions"
            ).versionRef("accompanist")
            library(
                "accompanist-systemuicontroller",
                "com.google.accompanist",
                "accompanist-systemuicontroller"
            ).versionRef("accompanist")
            library(
                "accompanist-navigation-animation",
                "com.google.accompanist",
                "accompanist-navigation-animation"
            ).versionRef("accompanist")

            // Better times
            library(
                "threeten.abp",
                "com.jakewharton.threetenabp",
                "threetenabp"
            ).versionRef("threeten")
            // HTML parsing
            library("jsoup", "org.jsoup", "jsoup").versionRef("jsoup")
            library("tagsoup", "org.ccil.cowan.tagsoup", "tagsoup").versionRef("tagsoup")
            // RSS
            library("rome", "com.rometools", "rome").versionRef("rome")
            library("rome-modules", "com.rometools", "rome-modules").versionRef("rome")

            // For better fetching
            library("okhttp", "com.squareup.okhttp3", "okhttp").withoutVersion()
            library("okio", "com.squareup.okio", "okio").version {
                strictly(okioVersion)
            }
            // For supporting TLSv1.3 on pre Android-10
            library(
                "conscrypt-android",
                "org.conscrypt",
                "conscrypt-android"
            ).versionRef("conscrypt")

            bundle("okhttp", listOf("okhttp", "okio"))
            bundle("okhttp-android", listOf("okhttp", "okio", "conscrypt-android"))

            // Image loading
            library("coil-base", "io.coil-kt", "coil-base").withoutVersion()
            library("coil-gif", "io.coil-kt", "coil-gif").withoutVersion()
            library("coil-svg", "io.coil-kt", "coil-svg").withoutVersion()
            library("coil-compose", "io.coil-kt", "coil-compose").withoutVersion()

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").version {
                strictly(kotlinVersion)
            }
            library(
                "kotlin-stdlib-common",
                "org.jetbrains.kotlin",
                "kotlin-stdlib-common"
            ).version {
                strictly(kotlinVersion)
            }
            bundle("kotlin-stdlib", listOf("kotlin-stdlib", "kotlin-stdlib-common"))
            library(
                "kotlin-test-junit",
                "org.jetbrains.kotlin",
                "kotlin-test-junit"
            ).versionRef("kotlin")
            // Coroutines
            library(
                "kotlin-coroutines-test",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-test"
            ).versionRef("coroutines")
            library(
                "kotlin-coroutines-core",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).versionRef("coroutines")
            // For doing coroutines on UI thread
            library(
                "kotlin-coroutines-android",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-android"
            ).versionRef("coroutines")
            // Dependency injection
            library(
                "kodein-androidx",
                "org.kodein.di",
                "kodein-di-framework-android-x"
            ).versionRef("kodein")
            // Custom tabs
            library("androidx-browser", "androidx.browser", "browser").versionRef("androidxBrowser")
            // Full text
            library(
                "readability4j",
                "net.dankito.readability4j",
                "readability4j"
            ).versionRef("readability4j")
            // For feeder-sync
            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit")
            library(
                "retrofit-converter-moshi",
                "com.squareup.retrofit2",
                "converter-moshi"
            ).versionRef("retrofit")
            library("moshi", "com.squareup.moshi", "moshi").versionRef("moshi")
            library("moshi-kotlin", "com.squareup.moshi", "moshi-kotlin").versionRef("moshi")
            library("moshi-adapters", "com.squareup.moshi", "moshi-adapters").versionRef("moshi")
            library("qrgen", "com.github.kenglxn.qrgen", "android").versionRef("qrgen")

            // testing
            library("junit", "junit", "junit").versionRef("junit")
            library("mockito-core", "org.mockito", "mockito-core").versionRef("mockito")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("mockwebserver", "com.squareup.okhttp3", "mockwebserver").versionRef("okhttp")
            library("threeten.bp", "org.threeten", "threetenbp").versionRef("threeten")

            library("mockk-android", "io.mockk", "mockk-android").versionRef("mockk")
            library("androidx-test-core", "androidx.test", "core").versionRef("androidxTestcore")
            library("androidx-test-core-ktx", "androidx.test", "core-ktx").versionRef("androidxTestcore")
            library("androidx-test-runner", "androidx.test", "runner").versionRef("testRunner")
            library("room-testing", "androidx.room", "room-testing").versionRef("room")
            library(
                "espresso-core",
                "androidx.test.espresso",
                "espresso-core"
            ).versionRef("espresso")
            library("compose-ui-test-junit4", "androidx.compose.ui", "ui-test-junit4").withoutVersion()
            library("compose-ui-test-manifest", "androidx.compose.ui", "ui-test-manifest").withoutVersion()
            library("androidx-test-junit-ktx", "androidx.test.ext", "junit-ktx").versionRef("androidx-test-junit-ktx")
        }
    }
}

rootProject.name = "feeder"

include(":app")
include(":jsonfeed-parser")

includeBuild("rome")
