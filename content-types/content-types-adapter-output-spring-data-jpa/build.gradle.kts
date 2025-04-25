// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework:spring-context")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("dev.forkhandles:values4k")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63")
    implementation("org.hibernate.orm:hibernate-core")
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:pagination"))
    runtimeOnly(project(":graph:graph-adapter-input-rest-spring-mvc")) // for thing serialization
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // for timestamp serialization
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly(project(":migrations:liquibase"))
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-orm")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
