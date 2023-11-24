// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":community:community-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.javax.activation)
}
