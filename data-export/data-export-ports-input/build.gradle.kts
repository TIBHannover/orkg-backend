plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-model-api")
    api(project(":common:core-identifiers"))
}
