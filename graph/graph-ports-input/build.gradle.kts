plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))

    api("org.eclipse.rdf4j:rdf4j-util")
    api("org.springframework.data:spring-data-commons")
}
