rootProject.name = "orkg-prototype"

include("platform")
include("library")
include(
    // The module containing the domain code will be called "application", because it will also contain application
    // services. This name might also reflect better that we may be able to split it out as a separate application (or
    // "microservice", if you prefer.) Also, we will share the ports from within the domain project for the time being.
    // This may change in the future.
    "graph:application",
    "graph:adapter-input-rest-spring-mvc",
    "graph:adapter-output-spring-data-neo4j-ogm",
    "graph:adapter-output-in-memory",
)
include("rest-api-server")

// Declare default repository settings for all sub-projects.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Warn if sub-projects declare their own repositories.
    repositories {
        mavenCentral()
    }
}
