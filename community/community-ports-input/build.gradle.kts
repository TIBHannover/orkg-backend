// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api(libs.javax.activation)
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":identity-management:idm-core-model")) // for LegacyController/UseCases, TODO: break dependency
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":graph:graph-core-model"))
}
