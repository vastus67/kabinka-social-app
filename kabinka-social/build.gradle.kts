plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "app.kabinka.social"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // BuildConfig fields for compatibility
        buildConfigField("String", "VERSION_NAME", "\"1.0.0\"")
        buildConfigField("int", "VERSION_CODE", "1")
    }
    
    flavorDimensions += "appMode"
    productFlavors {
        create("frontend") {
            dimension = "appMode"
            isDefault = true
        }
        create("legacy") {
            dimension = "appMode"
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
        create("profile") {
            initWith(getByName("debug"))
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
    
    lint {
        abortOnError = false
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Mastodon Dependencies (from original build.gradle)
    api("androidx.annotation:annotation:1.3.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("androidx.browser:browser:1.8.0")
    
    // Use LiteX libraries with androidx.recyclerview
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("me.grishka.litex:recyclerview:1.2.1.1")
    implementation("me.grishka.litex:swiperefreshlayout:1.2.0-beta01")
    implementation("me.grishka.litex:browser:1.4.0") {
        exclude(group = "androidx.browser", module = "browser")
    }
    implementation("me.grishka.litex:dynamicanimation:1.1.0-alpha03") {
        exclude(group = "androidx.dynamicanimation", module = "dynamicanimation")
    }
    implementation("me.grishka.litex:viewpager:1.0.0") {
        exclude(group = "androidx.viewpager", module = "viewpager")
    }
    implementation("me.grishka.litex:viewpager2:1.0.0") {
        exclude(group = "androidx.viewpager2", module = "viewpager2")
    }
    implementation("me.grishka.litex:palette:1.0.0")
    implementation("me.grishka.litex:concurrent:1.1.0") {
        exclude(group = "androidx.concurrent", module = "concurrent-futures")
    }
    implementation("me.grishka.litex:collection:1.1.0") {
        exclude(group = "androidx.collection", module = "collection")
    }
    implementation("me.grishka.appkit:appkit:1.4.7")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.squareup:otto:1.3.8")
    implementation("de.psdev:async-otto:1.0.3")
    implementation("com.google.zxing:core:3.5.3")
    implementation("org.microg:safe-parcel:1.5.0")
    implementation("org.parceler:parceler-api:1.1.13")
    annotationProcessor("org.parceler:parceler:1.1.13")
    kapt("org.parceler:parceler:1.1.13")
    
    // Network (modern versions for compatibility)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    
    // Storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")
    
    // WebSocket
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    
    // Core library desugaring for Java 21 compatibility
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    
    // Flutter embedding for FluffyChat integration
    val flutterRoot = System.getenv("USERPROFILE") + "\\flutter"
    compileOnly(files("$flutterRoot\\bin\\cache\\artifacts\\engine\\android-arm-release\\flutter.jar"))
}