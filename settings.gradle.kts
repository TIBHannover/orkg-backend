rootProject.name = "orkg-prototype"

include("platform")
include("library")
include("common:exceptions")
include("testing:kotest")
include("testing:spring")
include(
    // The module containing the domain code will be called "application", because it will also contain application
    // services. This name might also reflect better that we may be able to split it out as a separate application (or
    // "microservice", if you prefer.) Also, we will share the ports from within the domain project for the time being.
    // This may change in the future.
    "graph:graph-application",
    "graph:graph-adapter-input-rest-spring-mvc",
    "graph:graph-adapter-output-spring-data-neo4j-ogm",
    "graph:graph-adapter-output-spring-data-neo4j-sdn6",
    "graph:graph-adapter-output-in-memory",
)
include(
    "identity-management:idm-application",
    "identity-management:idm-adapter-output-spring-data-jpa",
)
include(
    ":discussions:discussions-adapter-output-spring-data-jpa-postgres",
)
include(
    ":media-storage:media-storage-adapter-output-spring-data-jpa-postgres",
)
include(
    ":feature-flags:feature-flags-ports",
    ":feature-flags:feature-flags-adapter-output-spring-properties",
)
include(
    "rdf-export:rdf-export-application",
    "rdf-export:rdf-export-adapter-input-rest-spring-mvc",
)
include("widget")
include("rest-api-server")

// Declare default repository settings for all sub-projects.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Warn if sub-projects declare their own repositories.
    repositories {
        mavenCentral()
    }
}
