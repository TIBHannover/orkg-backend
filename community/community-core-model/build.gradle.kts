// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework:spring-web")
    api(project(":common"))
    api(project(":media-storage:media-storage-core-model"))
    implementation(project(":graph:graph-core-model"))
    runtimeOnly(libs.jackson.databind)

    testFixturesApi(project(":common"))
    testFixturesApi(project(":media-storage:media-storage-core-model"))
    testFixturesImplementation(project(":graph:graph-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":community:community-core-model"))
            }
        }
    }
}
