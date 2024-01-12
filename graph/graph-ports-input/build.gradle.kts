plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":graph:graph-core-model"))

    implementation(project(":common"))
    implementation(project(":community:community-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.result4k) // for class use cases, TODO: refactor?
    implementation(libs.javax.activation)
}
