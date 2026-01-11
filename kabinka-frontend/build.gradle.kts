plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "app.kabinka.frontend"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.kabinka.frontend"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // AppAuth redirect scheme for OAuth callbacks
        manifestPlaceholders["appAuthRedirectScheme"] = "kabinka"

        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    flavorDimensions += "appMode"
    productFlavors {
        create("frontend") {
            dimension = "appMode"
            isDefault = true
        }
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
    
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "me.grishka.litex") {
                    when (requested.name) {
                        "recyclerview" -> useTarget("androidx.recyclerview:recyclerview:1.3.2")
                        "collection" -> useTarget("androidx.collection:collection:1.4.2")
                        "concurrent" -> useTarget("androidx.concurrent:concurrent-futures:1.1.0")
                        "viewpager" -> useTarget("androidx.viewpager:viewpager:1.0.0")
                        "viewpager2" -> useTarget("androidx.viewpager2:viewpager2:1.0.0")
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            pickFirsts += "**/*.so"
        }
    }
}

dependencies {
    implementation(project(":core-ui"))
    
    // Kabinka Social integration
    implementation(project(":kabinka-social")) {
        exclude(group = "me.grishka.litex")
        exclude(group = "me.grishka.appkit")
    }
    
    // AppKit for Callback/ErrorResponse
    implementation("me.grishka.appkit:appkit:1.4.7") {
        isTransitive = false
    }
    
    // Standard androidx libraries
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.collection:collection:1.4.2")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")
    
    // Core library desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    
    // Parceler
    implementation("org.parceler:parceler-api:1.1.13")
    
    // Compose BOM and UI
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Security for encrypted storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Feather Icons for Compose
    implementation("br.com.devsrsouza.compose.icons:feather:1.1.0")
    
    // Line Awesome Icons for Compose
    implementation("br.com.devsrsouza.compose.icons:line-awesome:1.1.0")
    
    // AppAuth for OAuth
    implementation("net.openid:appauth:0.11.1")
    
    // OkHttp and Retrofit for Mastodon API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Browser support for Custom Tabs
    implementation("androidx.browser:browser:1.7.0")

    debugImplementation(libs.compose.ui.tooling)
}
