// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    api(project(":community:community-core-model"))

    implementation(project(":common"))
    implementation(project(":identity-management:idm-core-model")) // for LegacyController/UseCases, TODO: break dependency
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.javax.activation)
}
