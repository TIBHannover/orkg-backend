// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    //kotlin("jvm") // TODO: remove on upgrade
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":graph:graph-application"))
    implementation(project(":profiling:profiling-application"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}
