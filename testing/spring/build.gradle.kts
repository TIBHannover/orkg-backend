// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    testFixturesApi(libs.bundles.testcontainers)
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")

    // TODO: These might be "downgraded" to only use the specific package used to declare the API
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "org.springframework.data", module = "spring-data-commons")
        exclude(group = "org.springframework.data", module = "spring-data-jpa")
    }

    testFixturesCompileOnly(libs.spring.boot.starter.neo4j.migrations)
    testFixturesImplementation("org.springframework.data:spring-data-commons")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testFixturesImplementation(libs.spring.mockk)
    testFixturesImplementation(libs.spring.restdocs)
    testFixturesImplementation("org.springframework.security:spring-security-core")
    testFixturesImplementation("org.springframework.security:spring-security-test")
}
