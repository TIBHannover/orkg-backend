plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":licenses:licenses-core-model"))
    api(project(":licenses:licenses-ports-input"))
    implementation("org.slf4j:slf4j-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
            }
        }
    }
}
