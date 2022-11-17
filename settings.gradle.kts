rootProject.name = "orkg-prototype"

include("platform")
include("library")
include("rest-api-server")

// Declare default repository settings for all sub-projects.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Warn if sub-projects declare their own repositories.
    repositories {
        mavenCentral()
    }
}
