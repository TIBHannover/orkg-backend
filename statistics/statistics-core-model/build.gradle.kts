// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common"))

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
            }
        }
    }
}
