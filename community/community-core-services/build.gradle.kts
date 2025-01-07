// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api("org.springframework:spring-tx")
    api(project(":common:identifiers"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output"))
    api(project(":graph:graph-ports-output"))
    api(project(":media-storage:media-storage-core-model"))
    api(project(":media-storage:media-storage-ports-input"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":graph:graph-core-model")) // for ResearchFields
    implementation("org.springframework:spring-core")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":common:pagination"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.eclipse.rdf4j:rdf4j-common-io")
                implementation("org.junit.jupiter:junit-jupiter-api")
            }
        }
    }
}
