// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":common:identifiers"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation(project(":common:pagination"))
    implementation(project(":common:datatypes"))

    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api("org.springframework:spring-tx")
    implementation("dev.forkhandles:values4k")
    implementation(project(":community:community-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-api")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.assertj:assertj-core")
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring"))) // for fixedClock
                runtimeOnly("io.kotest:kotest-runner-junit5")
            }
        }
    }
}
