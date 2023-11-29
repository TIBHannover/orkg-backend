plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }
}
