// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    implementation("org.springframework:spring-web")
    implementation("dev.forkhandles:values4k")
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:spring-webmvc"))
    implementation(project(":integrations:datacite-serialization"))
    implementation("net.handle:handle-client")
    implementation("org.slf4j:slf4j-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":common:spring-webmvc")))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
            }
        }
    }
}
