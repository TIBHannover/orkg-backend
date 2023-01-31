// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.plugin.SpringBootPlugin


plugins {
    id("org.orkg.kotlin-conventions")
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
                implementation(testFixtures(project(":graph:application"))) {
                    exclude(group = "org.neo4j", module = "neo4j-ogm-bolt-native-types")
                }
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("com.ninja-squad:springmockk:2.0.1")
            }
        }
    }
}

dependencies {
    api(enforcedPlatform(SpringBootPlugin.BOM_COORDINATES)) // TODO: align with platform when upgrade is done

    // TODO: remove when domain was moved
    api(project(":library")) {
        exclude(group = "org.neo4j", module = "neo4j-ogm-bolt-native-types")
    }
    api(project(":graph:application"))

    // Pagination (e.g. Page, Pageable, etc.)
    api("org.springframework.data:spring-data-commons")

    // Forkhandles
    implementation(libs.forkhandles.values4k)

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    // Caching
    api("org.springframework.boot:spring-boot-starter-cache")
}
