// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":profiling:profiling-core-services"))
    api(project(":profiling:profiling-ports-output"))
    implementation("org.neo4j.driver:neo4j-java-driver")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":content-types:content-types-ports-output"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":statistics:statistics-ports-output"))
}
