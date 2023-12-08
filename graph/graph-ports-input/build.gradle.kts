plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spotless)
    id("org.orkg.neo4j-conventions") // to obtain version of spring-data commons. TODO: remove after upgrade
}

dependencies {
    api(platform(project(":platform")))

    api(project(":graph:graph-core-model"))

    implementation(project(":common"))
    implementation(project(":community:community-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.result4k) // for class use cases, TODO: refactor?
    implementation(libs.javax.activation)
}
