// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    kotlin("plugin.spring")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common:exceptions"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.security:spring-security-crypto") // for PasswordEncoder
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
