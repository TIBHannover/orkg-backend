// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation(project(":statistics:statistics-ports-input"))
    implementation(project(":statistics:statistics-core-model"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":community:community-ports-output"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.databind)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation(libs.spring.mockk)
                implementation("org.assertj:assertj-core")
                implementation(testFixtures(project(":statistics:statistics-core-model")))
            }
        }
    }
}
