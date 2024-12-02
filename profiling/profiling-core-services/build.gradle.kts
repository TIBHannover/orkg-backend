// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api(project(":profiling:profiling-ports-output"))
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // TODO: Can be removed after organization refactoring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(kotlin("reflect"))
    implementation(project(":profiling:profiling-core-model"))
}
