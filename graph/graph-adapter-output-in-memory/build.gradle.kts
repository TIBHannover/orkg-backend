// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")


plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:graph-application")))
            }
        }
    }
}

dependencies {
    implementation(project(":graph:graph-application"))
    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.forkhandles.values4k)
}
