// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")


plugins {
    id("org.orkg.kotlin-conventions")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

dependencies {
    api(platform(project(":platform")))

    api(project(":graph:application"))
}
