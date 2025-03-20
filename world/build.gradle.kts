plugins {
    kotlin("jvm") version "2.1.10"
    jacoco
}

group = "org.orkg"
version = "0.1.0-SNAPSHOT"

java {
    // Configure runtime environment explicitly, otherwise the Compose Gradle plugin fails.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    api(platform("org.orkg:platform"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
