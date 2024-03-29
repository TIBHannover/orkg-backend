// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":identity-management:idm-ports-output"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":community:community-adapter-output-spring-data-jpa"))
    implementation(project(":community:community-ports-output"))
    implementation(project(":community:community-core-model"))
    implementation(project(":profiling:profiling-core-model"))
    implementation(project(":profiling:profiling-ports-output"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // TODO: Can be removed after orgnization refactoring

    implementation(kotlin("reflect"))
}
