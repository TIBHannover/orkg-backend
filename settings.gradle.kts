rootProject.name = "orkg-prototype"

include("platform")
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
    "graph:graph-adapter-output-spring-data-neo4j-sdn6",
    "graph:graph-adapter-output-in-memory",
)
include(
    "identity-management:idm-application",
    "identity-management:idm-adapter-input-rest-spring-security",
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
    "data-export:data-export-application",
    "data-export:data-export-adapter-input-rest-spring-mvc",
)
include(
    "licenses:licenses-application",
    "licenses:licenses-adapter-input-rest-spring-mvc",
    "licenses:licenses-adapter-output-spring",
)
include(
    "profiling:profiling-application",
    "profiling:profiling-adapter-output-spring-data-neo4j-sdn6"
)
include("widget")
include("rest-api-server")

// Declare default repository settings for all sub-projects.
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
