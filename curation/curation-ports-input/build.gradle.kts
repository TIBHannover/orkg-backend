plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":curation:curation-core-model"))
    api(project(":graph:graph-core-model"))
}
