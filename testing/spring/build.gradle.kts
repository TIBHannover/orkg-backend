// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    testFixturesApi(libs.bundles.testcontainers)
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")

    // TODO: These might be "downgraded" to only use the specific package used to declare the API
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa"){
        exclude(group = "org.springframework.data", module ="spring-data-commons")
        exclude(group = "org.springframework.data", module="spring-data-jpa")
    }

    testFixturesImplementation("org.springframework.data:spring-data-commons:2.6.10")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testFixturesImplementation(libs.spring.mockk)
    testFixturesImplementation(libs.spring.restdocs)
    testFixturesApi("com.github.dasniko:testcontainers-keycloak:3.0.0")
}
