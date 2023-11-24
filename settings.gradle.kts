rootProject.name = "orkg-backend"

include("platform")
include(
    "common",
    "common:serialization",
    "common:neo4j-dsl",
)
include(
    "migrations:liquibase",
    "migrations:neo4j-migrations"
)
include("testing:kotest")
include("testing:spring")
include(
    // The module containing the domain code will be called "application", because it will also contain application
    // services. This name might also reflect better that we may be able to split it out as a separate application (or
    // "microservice", if you prefer.) Also, we will share the ports from within the domain project for the time being.
    // This may change in the future.
    "graph:graph-core-model",
    "graph:graph-core-services",
    "graph:graph-ports-input",
    "graph:graph-ports-output",
    "graph:graph-adapter-input-rest-spring-mvc",
    "graph:graph-adapter-output-spring-data-neo4j-sdn6",
    "graph:graph-adapter-output-in-memory",
)
include(
    "content-types:content-types-core-services",
    "content-types:content-types-core-model",
    "content-types:content-types-ports-input",
    "content-types:content-types-ports-output",
    "content-types:content-types-adapter-input-rest-spring-mvc",
    "content-types:content-types-adapter-output-web",
    "content-types:content-types-adapter-output-spring-data-neo4j-sdn6",
)
include(
    "community:community-core-services",
    "community:community-core-model",
    "community:community-ports-input",
    "community:community-ports-output",
    "community:community-adapter-input-rest-spring-mvc",
    "community:community-adapter-output-spring-data-jpa",
)
include(
    "identity-management:idm-core-services",
    "identity-management:idm-core-model",
    "identity-management:idm-ports-input",
    "identity-management:idm-ports-output",
    "identity-management:idm-adapter-input-rest-spring-security",
    "identity-management:idm-adapter-output-spring-data-jpa",
)
include(
    ":discussions:discussions-adapter-input-rest-spring-mvc",
    ":discussions:discussions-adapter-output-spring-data-jpa",
    ":discussions:discussions-core-model",
    ":discussions:discussions-core-services",
    ":discussions:discussions-ports-input",
    ":discussions:discussions-ports-output",
)
include(
    ":media-storage:media-storage-adapter-input-serialization",
    ":media-storage:media-storage-adapter-output-spring-data-jpa",
    ":media-storage:media-storage-core-model",
    ":media-storage:media-storage-core-services",
    ":media-storage:media-storage-ports-input",
    ":media-storage:media-storage-ports-output",
)
include(
    ":feature-flags:feature-flags-ports",
    ":feature-flags:feature-flags-adapter-output-spring-properties",
)
include(
    "data-export:data-export-ports-input",
    "data-export:data-export-core",
    "data-export:data-export-adapters",
)
include(
    "licenses:licenses-core-model",
    "licenses:licenses-core-services",
    "licenses:licenses-ports-input",
    "licenses:licenses-adapter-input-rest-spring-mvc",
)
include(
    "profiling:profiling-core-model",
    "profiling:profiling-core-services",
    "profiling:profiling-ports-output",
    "profiling:profiling-adapter-output",
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
