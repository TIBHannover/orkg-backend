// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":content-types:content-types-ports-output"))
    implementation(project(":profiling:profiling-core-model"))
    implementation(project(":profiling:profiling-core-services"))
    implementation(project(":profiling:profiling-ports-output"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}
