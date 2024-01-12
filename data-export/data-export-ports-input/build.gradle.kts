plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }
}
