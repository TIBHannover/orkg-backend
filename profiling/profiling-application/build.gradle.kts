// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    alias(libs.plugins.spotless)
    kotlin("plugin.spring")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":graph:graph-application"))
    implementation(project(":identity-management:idm-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // TODO: Can be removed after orgnization refactoring

    implementation(kotlin("reflect"))
}
