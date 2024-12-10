// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
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
    api("org.springframework:spring-tx")
    implementation(project(":common:string-utils"))
    api(project(":common:identifiers"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-output"))
    api(project(":eventbus"))
    api("org.springframework:spring-beans")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.assertj:assertj-core")
                runtimeOnly(project(":migrations:liquibase"))
                runtimeOnly("org.liquibase:liquibase-core")
                runtimeOnly("org.postgresql:postgresql")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(testFixtures(project(":community:community-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
