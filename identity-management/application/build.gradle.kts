// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    id("java-test-fixtures")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common:exceptions"))

    api("org.springframework:spring-context")
    api("org.springframework:spring-tx")
    implementation("org.springframework.security:spring-security-crypto") // for PasswordEncoder
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("orkg${project.path}".replace(":", "-"))
}
