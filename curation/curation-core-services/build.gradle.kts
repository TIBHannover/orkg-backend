// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":community:community-ports-output"))
    api(project(":curation:curation-core-model"))
    api(project(":curation:curation-ports-input"))
    api(project(":curation:curation-ports-output"))
}
