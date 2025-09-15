plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("dev.forkhandles:values4k")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("io.kotest:kotest-runner-junit5")
            }
        }
    }
}
