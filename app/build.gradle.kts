import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.notisprite.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.notisprite.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        checkDependencies = true
        warningsAsErrors = false
        baseline = file("lint-baseline.xml")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// ktlint runs from the CLI artifact instead of the Gradle plugin so it stays
// independent of the AGP / Gradle version pair.
val ktlint by configurations.creating

val ktlintCheck by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Checks Kotlin sources with ktlint."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "src/main/kotlin",
        "src/test/kotlin",
        "src/androidTest/kotlin",
        "build.gradle.kts",
    )
    isIgnoreExitValue = false
}

val ktlintFormat by tasks.registering(JavaExec::class) {
    group = "formatting"
    description = "Formats Kotlin sources with ktlint."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args(
        "-F",
        "src/main/kotlin",
        "src/test/kotlin",
        "src/androidTest/kotlin",
        "build.gradle.kts",
    )
}

tasks.named("check") {
    dependsOn(ktlintCheck)
}

dependencies {
    ktlint("com.pinterest.ktlint:ktlint-cli:1.8.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
