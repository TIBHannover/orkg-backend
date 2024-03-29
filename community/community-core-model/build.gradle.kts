// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    implementation(project(":common"))

    implementation(project(":media-storage:media-storage-core-model"))
    implementation(project(":graph:graph-core-model"))

    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core) // for JsonProperty

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":media-storage:media-storage-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
