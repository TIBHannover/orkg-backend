// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":community:community-ports-input")) // used in LegacyPaperService
    implementation(project(":community:community-ports-output"))

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
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":content-types:content-types-ports-input")))
                implementation(project(":media-storage:media-storage-core-model"))
                implementation("org.assertj:assertj-core")
                implementation(libs.kotest.runner)
                implementation(libs.spring.mockk)
            }
        }
    }
}
