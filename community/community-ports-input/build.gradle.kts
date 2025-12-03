// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.github.multiformats:java-multihash")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":community:community-core-model"))
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":graph:graph-core-model"))
}
