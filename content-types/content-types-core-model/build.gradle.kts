// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":community:community-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":content-types:content-types-ports-input"))
    testFixturesImplementation(project(":content-types:content-types-ports-output"))
    testFixturesImplementation(project(":content-types:content-types-core-services"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.assertj:assertj-core")
                implementation(libs.kotest.runner)
            }
        }
    }
}
