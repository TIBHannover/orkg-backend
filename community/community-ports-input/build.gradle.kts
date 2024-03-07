// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":community:community-core-model"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":identity-management:idm-core-model")) // for LegacyController/UseCases, TODO: break dependency
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.javax.activation)
}
