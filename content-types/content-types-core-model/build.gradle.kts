// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:datatypes"))
    api(project(":common:external-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-constants"))
    implementation("dev.forkhandles:values4k")

    testFixturesImplementation(project(":common:datatypes"))
    testFixturesApi("dev.forkhandles:fabrikate4k")
    testFixturesApi(project(":common:core-identifiers"))
    testFixturesApi(project(":common:external-identifiers"))
    testFixturesApi(project(":graph:graph-core-model"))
    testFixturesImplementation("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesImplementation("dev.forkhandles:values4k")
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
    testFixturesImplementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:serialization")))
            }
        }
    }
}
