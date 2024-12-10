@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api(project(":common:identifiers"))
    api(project(":discussions:discussions-core-model"))
    api(project(":discussions:discussions-ports-output"))
    implementation("jakarta.validation:jakarta.validation-api")
    runtimeOnly(project(":migrations:liquibase"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.springframework:spring-beans")
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(testFixtures(project(":discussions:discussions-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
