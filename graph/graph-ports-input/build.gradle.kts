plugins {
    id("org.orkg.kotlin-conventions")
    alias(libs.plugins.spotless)
    id("org.orkg.neo4j-conventions") // to obtain version of spring-data commons. TODO: remove after upgrade
}

dependencies {
    api(platform(project(":platform")))

    compileOnly(project(":common"))
    compileOnly(project(":graph:graph-core-model"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":community:community-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))
    // for PageRequests object

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.result4k) // for class use cases, TODO: refactor?
    implementation(libs.javax.activation)
}
