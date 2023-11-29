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
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":community:community-core-model"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":community:community-ports-output"))
    implementation(project(":community:community-adapter-output-spring-data-jpa"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":content-types:content-types-ports-input"))
    implementation(project(":content-types:content-types-ports-output"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.data:spring-data-jpa")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":content-types:content-types-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":content-types:content-types-ports-input")))
                implementation(project(":media-storage:media-storage-core-model"))
                implementation("org.assertj:assertj-core")
                implementation(libs.kotest.runner)
                implementation(libs.spring.mockk)
            }
        }
    }
}
