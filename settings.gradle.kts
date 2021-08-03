rootProject.name = "orkg-prototype"

include("platform")

include(
    ":application:shared",
    ":application:core"
)

include(":adapters:input:core")

include(":adapters:output:core")

include("rest-api-server")

// Declare default repository settings for all sub-projects.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Warn if sub-projects declare their own repositories.
    repositories {
        mavenCentral()
    }
}
