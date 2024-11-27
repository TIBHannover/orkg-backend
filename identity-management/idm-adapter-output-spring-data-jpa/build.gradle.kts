// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":identity-management:idm-ports-output"))

    api("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api(project(":identity-management:idm-core-model"))
    implementation("jakarta.validation:jakarta.validation-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
