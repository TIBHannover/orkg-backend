// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    //id("org.orkg.spring-restdocs-producer")
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(libs.forkhandles.fabrikate4k)
                implementation(libs.jackson.kotlin)
            }
        }
    }
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":identity-management:idm-ports-input"))

    implementation(project(":common")) // for exceptions

    implementation(libs.jackson.core)

    implementation("org.springframework.boot:spring-boot-starter-validation")

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
