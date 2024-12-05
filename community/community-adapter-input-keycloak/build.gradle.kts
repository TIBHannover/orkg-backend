// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    api("org.keycloak:keycloak-admin-client")
    api(project(":community:community-ports-output"))
    implementation(project(":common"))
    implementation(project(":community:community-core-model"))
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation("org.keycloak:keycloak-client-common-synced")
}
