// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")


plugins {
    id("org.orkg.kotlin-conventions")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:application")))
            }
        }
    }
}

dependencies {
    api(project(":graph:application"))
    api("org.springframework.data:spring-data-commons")
    implementation(libs.forkhandles.values4k)
}
