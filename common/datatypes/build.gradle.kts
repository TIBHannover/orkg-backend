// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io") // for RFC 3987 compliant IRIs
    implementation(project(":common:string-utils"))
    testFixturesApi("org.hamcrest:hamcrest")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-params")
            }
        }
    }
}
