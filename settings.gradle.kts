pluginManagement {
    includeBuild("gradle/meta-plugins")
}
plugins {
    id("org.orkg.gradle.settings")
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "orkg-backend"

include("documentation")
include(
    "common:testing",
    "common:serialization",
    "common:neo4j-dsl",
    "common:spring-data",
    "common:spring-webmvc",
    "common:identifiers",
    "common:pagination",
    "common:string-utils",
    "common:datatypes",
    "common:functional",
)
include(
    "migrations:liquibase",
    "migrations:neo4j-migrations",
)
include("keycloak")
include("testing:kotest")
include("testing:spring")
include(
    // The module containing the domain code will be called "application", because it will also contain application
    // services. This name might also reflect better that we may be able to split it out as a separate application (or
    // "microservice", if you prefer.) Also, we will share the ports from within the domain project for the time being.
    // This may change in the future.
    "graph:graph-core-constants",
    "graph:graph-core-model",
    "graph:graph-core-services",
    "graph:graph-ports-input",
    "graph:graph-ports-output",
    "graph:graph-adapter-input-rest-spring-mvc",
    "graph:graph-adapter-input-representations",
    "graph:graph-adapter-output-spring-data-neo4j",
    "graph:graph-adapter-output-in-memory",
    "graph:graph-adapter-output-web",
)
include(
    "content-types:content-types-core-services",
    "content-types:content-types-core-model",
    "content-types:content-types-ports-input",
    "content-types:content-types-ports-output",
    "content-types:content-types-adapter-input-rest-spring-mvc",
    "content-types:content-types-adapter-input-representations",
    "content-types:content-types-adapter-output-web",
    "content-types:content-types-adapter-output-simcomp",
    "content-types:content-types-adapter-output-spring-data-neo4j",
)
include(
    "community:community-core-services",
    "community:community-core-services-legacy",
    "community:community-core-model",
    "community:community-ports-input",
    "community:community-ports-input-legacy",
    "community:community-ports-output",
    "community:community-adapter-input-keycloak",
    "community:community-adapter-input-rest-spring-mvc",
    "community:community-adapter-input-rest-spring-mvc-legacy",
    "community:community-adapter-input-representations",
    "community:community-adapter-output-spring-data-jpa",
)
include(
    "identity-management:idm-adapter-input-rest-spring-security-legacy",
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
    "profiling:profiling-adapter-output-spring-data-neo4j",
)
include(
    "statistics:statistics-core-model",
    "statistics:statistics-core-services",
    "statistics:statistics-ports-input",
    "statistics:statistics-ports-output",
    "statistics:statistics-adapter-input-rest-spring-mvc",
    "statistics:statistics-adapter-input-representations",
    "statistics:statistics-adapter-output-spring-data-neo4j",
)
include(
    "curation:curation-core-services",
    "curation:curation-ports-input",
    "curation:curation-ports-output",
    "curation:curation-adapter-input-rest-spring-mvc",
    "curation:curation-adapter-output-spring-data-neo4j",
)
include("widget")
include("rest-api-server")
include(":integrations:datacite-serialization")

includeBuild("world")
