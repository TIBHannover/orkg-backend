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

    api(project(":identity-management:application"))

    api("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
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
