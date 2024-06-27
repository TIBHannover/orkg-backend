// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons:2.7.16")
    api("org.springframework:spring-context")
    api(project(":curation:curation-ports-input"))
    api(project(":curation:curation-ports-output"))
    api(project(":graph:graph-core-model"))
}
