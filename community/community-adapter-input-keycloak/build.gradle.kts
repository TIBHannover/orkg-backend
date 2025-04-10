// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.keycloak:keycloak-admin-client")
    api(project(":community:community-ports-output"))
    implementation(project(":common:identifiers"))
    implementation(project(":community:community-core-model"))
    implementation("jakarta.ws.rs:jakarta.ws.rs-api")
    implementation("org.springframework:spring-web")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("org.keycloak:keycloak-client-common-synced")
    implementation("org.slf4j:slf4j-api")
}
