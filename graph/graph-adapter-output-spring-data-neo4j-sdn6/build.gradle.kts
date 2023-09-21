// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.plugin.SpringBootPlugin


plugins {
    // id("org.orkg.kotlin-conventions") // TODO: re-apply after upgrading
    kotlin("jvm") // TODO: remove on upgrade
    id("org.springframework.boot") version "2.7.8" apply false
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-application")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
            }
        }
        val containerTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.FUNCTIONAL_TEST)
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(libs.kotest.extensions.spring)
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-application"))) {
                    exclude(group = "org.neo4j", module = "neo4j-ogm-bolt-native-types")
                    exclude(group = "org.liquibase", module = "liquibase-core") // Do not bring in forced version (via platform)
                }
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

dependencies {
    api(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES)) // TODO: align with platform when upgrade is done
    "containerTestApi"(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES)) // TODO: align with platform when upgrade is done

    implementation(project(":graph:graph-application"))

    // Pagination (e.g. Page, Pageable, etc.)
    implementation("org.springframework.data:spring-data-commons")

    // Forkhandles
    implementation(libs.forkhandles.values4k)

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")
}

tasks.named("check") {
    dependsOn(testing.suites.named("containerTest"))
}