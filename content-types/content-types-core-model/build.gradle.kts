// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.eclipse.rdf4j:rdf4j-util")
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api(libs.forkhandles.values4k)
    api(project(":common"))
    api(project(":graph:graph-core-model"))
    implementation("org.springframework:spring-web")

    testFixturesApi(project(":common"))
    testFixturesApi(project(":graph:graph-core-model"))
    testFixturesApi(libs.forkhandles.fabrikate4k)
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-util")
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(libs.kotest.runner)
                implementation(project(":content-types:content-types-core-model"))
            }
        }
    }
}
