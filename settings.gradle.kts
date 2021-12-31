rootProject.name = "Qu1ckr00t"
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "qu1ckr00t"
include(
    ":shared",
    ":app",
)

val serviceRoot = "service"
project(":interface").projectDir = file("$serviceRoot${File.separator}interface")
project(":service").projectDir = file("$serviceRoot${File.separator}service")

buildCache { local { removeUnusedEntriesAfterDays = 1 } }

include(":shared", ":app")
