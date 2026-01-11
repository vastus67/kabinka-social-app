plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
  namespace = "app.kabinka.core.ui"
  compileSdk = 34
  defaultConfig { minSdk = 26 }
  
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  
  kotlinOptions {
    jvmTarget = "17"
  }
  
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get() }
}

dependencies {
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.foundation)
  implementation(libs.compose.material3)
  implementation("androidx.compose.material:material-icons-extended")
  implementation("br.com.devsrsouza.compose.icons:feather:1.1.0")
  implementation("br.com.devsrsouza.compose.icons:line-awesome:1.1.0")
  implementation(libs.androidx.activity)
  implementation("io.coil-kt:coil-compose:2.5.0")
}
