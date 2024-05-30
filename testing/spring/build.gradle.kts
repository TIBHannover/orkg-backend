// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    testFixturesApi("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
    testFixturesApi("jakarta.persistence:jakarta.persistence-api")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi("org.springframework.boot:spring-boot-autoconfigure")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.boot:spring-boot-test-autoconfigure")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("org.springframework.security:spring-security-core")
    testFixturesApi("org.springframework.security:spring-security-test")
    testFixturesApi("org.springframework:spring-beans")
    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework:spring-test")
    testFixturesApi("org.springframework:spring-web")
    testFixturesApi(libs.jackson.databind)
    testFixturesApi(libs.spring.boot.starter.neo4j.migrations)
    testFixturesApi(libs.spring.restdocs)
    testFixturesApi(libs.testcontainers.junit5)
    testFixturesApi(libs.testcontainers.neo4j)
    testFixturesApi(libs.testcontainers.postgresql)
    testFixturesImplementation("org.hamcrest:hamcrest:2.2")
    testFixturesImplementation(libs.assertj.core)
    testFixturesImplementation(libs.restdocs.openapi) // FIXME: no idea why this works, but GAV does not
    testFixturesImplementation(libs.testcontainers.core)
}

dependencyAnalysis {
    issues {
        onUnusedDependencies {
            // We do not use the "main" source set, so the (automatically added) stdlib is always unused.
            exclude("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }
}
