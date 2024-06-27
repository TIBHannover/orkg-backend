// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
        }
    }
}
