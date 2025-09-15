// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    api(project(":statistics:statistics-core-model"))
    implementation("dev.forkhandles:values4k")
    implementation(project(":common:datatypes"))
    implementation(project(":common:pagination"))
    implementation(project(":community:community-core-model"))
    implementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-engine")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.assertj:assertj-core")
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":graph:graph-ports-input")))
                implementation(testFixtures(project(":testing:spring"))) // for fixedClock
                implementation(project(":media-storage:media-storage-core-model"))
                runtimeOnly("io.kotest:kotest-runner-junit5")
            }
        }
    }
}
