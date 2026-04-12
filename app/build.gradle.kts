import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val secondBloomLocalProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val debugRemodelApiBaseUrl = (
    providers.gradleProperty("secondBloomRemodelApiBaseUrl").orNull
        ?: secondBloomLocalProperties.getProperty("secondBloomRemodelApiBaseUrl")
).orEmpty().trim()

val debugClerkPublishableKey = (
    providers.gradleProperty("secondBloomClerkPublishableKey").orNull
        ?: secondBloomLocalProperties.getProperty("secondBloomClerkPublishableKey")
).orEmpty().trim()

val debugClerkJwtTemplate = (
    providers.gradleProperty("secondBloomClerkJwtTemplate").orNull
        ?: secondBloomLocalProperties.getProperty("secondBloomClerkJwtTemplate")
).orEmpty().trim()

val escapedDebugRemodelApiBaseUrl = debugRemodelApiBaseUrl
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

val escapedDebugClerkPublishableKey = debugClerkPublishableKey
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

val escapedDebugClerkJwtTemplate = debugClerkJwtTemplate
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.scf.secondbloom"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.scf.secondbloom"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "REMODEL_API_BASE_URL", "\"\"")
        buildConfigField("boolean", "REMODEL_USE_REAL_API", "false")
        buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"$escapedDebugClerkPublishableKey\"")
        buildConfigField("String", "CLERK_JWT_TEMPLATE", "\"$escapedDebugClerkJwtTemplate\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            // Allow local debug builds to opt into the real backend without editing tracked files.
            buildConfigField(
                "String",
                "REMODEL_API_BASE_URL",
                "\"$escapedDebugRemodelApiBaseUrl\""
            )
            buildConfigField(
                "boolean",
                "REMODEL_USE_REAL_API",
                debugRemodelApiBaseUrl.isNotBlank().toString()
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.clerk.android.ui)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
