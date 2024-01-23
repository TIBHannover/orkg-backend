plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))

    api("org.springframework.data:spring-data-commons")
    api(libs.forkhandles.result4k) // for class use cases, TODO: refactor?
}
