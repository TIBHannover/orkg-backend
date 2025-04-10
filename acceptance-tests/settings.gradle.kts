plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "acceptance-tests"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("testing-dsl")

includeBuild("../world")
