// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")


plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))

    api("org.springframework.data:spring-data-commons")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":graph:graph-ports-output")))
                implementation("io.kotest:kotest-framework-api")
                runtimeOnly(libs.kotest.runner)
            }
        }
    }
}
