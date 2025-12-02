// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:core-identifiers"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    implementation(project(":common:pagination"))
    implementation(project(":common:spring-webmvc"))
    implementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":graph:graph-ports-output")))
                implementation("io.kotest:kotest-framework-engine")
                runtimeOnly("io.kotest:kotest-runner-junit5")
            }
        }
    }
}
