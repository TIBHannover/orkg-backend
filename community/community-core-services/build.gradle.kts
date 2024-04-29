// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output"))

    implementation(project(":common"))
    implementation(project(":identity-management:idm-ports-output")) // for UserRepository, TODO: break dependency
    implementation(project(":graph:graph-core-model")) // for ResearchFields
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":media-storage:media-storage-ports-input"))
    implementation(project(":media-storage:media-storage-core-model"))

    implementation("org.springframework:spring-context")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":media-storage:media-storage-core-model")))
                implementation("org.assertj:assertj-core")
                implementation(libs.spring.mockk)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
