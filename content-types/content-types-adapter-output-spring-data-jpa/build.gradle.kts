// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("jakarta.persistence:jakarta.persistence-api")
    api("tools.jackson.core:jackson-databind")
    api("org.springframework.boot:spring-boot-persistence")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-context")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    implementation("tools.jackson.core:jackson-core")
    implementation("dev.forkhandles:values4k")
    implementation("io.hypersistence:hypersistence-utils-hibernate-73")
    implementation("org.hibernate.orm:hibernate-core")
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:pagination"))
    runtimeOnly(project(":graph:graph-adapter-input-rest-spring-mvc")) // for thing serialization
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                runtimeOnly("org.springframework.boot:spring-boot-data-jpa-test")
                runtimeOnly("org.springframework.boot:spring-boot-liquibase")
                runtimeOnly(project(":migrations:liquibase"))
                compileOnly("org.junit.jupiter:junit-jupiter-api")
                compileOnly("org.springframework.boot:spring-boot-data-jpa-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-orm")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-jackson")
                implementation("org.springframework.boot:spring-boot-jdbc-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
