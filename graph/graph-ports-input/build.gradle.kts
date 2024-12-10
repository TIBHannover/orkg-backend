plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common:identifiers"))
    api(project(":graph:graph-core-model"))

    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
}
