// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":community:community-ports-output"))
    api(project(":statistics:statistics-core-model"))
    api(project(":statistics:statistics-ports-input"))
    api(project(":statistics:statistics-ports-output"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(testFixtures(project(":statistics:statistics-core-model")))
            }
        }
    }
}
