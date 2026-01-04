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
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }

    }
}

rootProject.name = "kabinka-social-app"

// Active modules
include(":kabinka-social")
include(":kabinka-frontend")
include(":core-ui")

// bolt-new-mockup is included as reference only (not built)
// See bolt-new-mockup/ folder for frontend implementation examples
