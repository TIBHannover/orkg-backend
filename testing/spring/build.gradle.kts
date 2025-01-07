plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    testFixturesImplementation(project(":constants"))
    testFixturesApi("com.github.dasniko:testcontainers-keycloak")
    testFixturesApi("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
    testFixturesApi("org.junit.jupiter:junit-jupiter-api")
    testFixturesApi("org.springframework.boot:spring-boot-autoconfigure")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.boot:spring-boot-test-autoconfigure")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("org.springframework.security:spring-security-config")
    testFixturesApi("org.springframework.security:spring-security-core")
    testFixturesApi("org.springframework.security:spring-security-test")
    testFixturesApi("org.springframework.security:spring-security-web")
    testFixturesApi("org.springframework.security:spring-security-web")
    testFixturesApi("org.springframework:spring-beans")
    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework:spring-core")
    testFixturesApi("org.springframework:spring-test")
    testFixturesApi("org.springframework.data:spring-data-commons")
    testFixturesApi("com.fasterxml.jackson.core:jackson-databind")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-mockmvc")
    testFixturesApi("org.testcontainers:junit-jupiter")
    testFixturesApi("org.testcontainers:neo4j")
    testFixturesApi("org.testcontainers:postgresql")
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":common:testing")))
    testFixturesImplementation("com.epages:restdocs-api-spec-mockmvc")
    testFixturesImplementation("org.hamcrest:hamcrest")
    testFixturesImplementation("org.springframework.security:spring-security-crypto")
    testFixturesImplementation("org.springframework:spring-web")
    testFixturesImplementation("org.testcontainers:testcontainers")
}
