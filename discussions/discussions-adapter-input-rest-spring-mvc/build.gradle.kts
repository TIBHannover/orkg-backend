// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.spring-restdocs-producer")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(project(":identity-management:idm-ports-input"))
                implementation(project(":identity-management:idm-ports-output"))
                implementation(testFixtures(project(":identity-management:idm-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(libs.forkhandles.fabrikate4k)
            }
        }
    }
}

dependencies {
    api(platform(project(":platform")))

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7

    implementation(project(":common"))
    implementation(project(":common:serialization"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))
    implementation(project(":discussions:discussions-core-model"))
    implementation(project(":discussions:discussions-core-services"))
    implementation(project(":discussions:discussions-ports-input"))
    implementation(project(":community:community-ports-output"))
    implementation(project(":community:community-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.databind)

    implementation("jakarta.validation:jakarta.validation-api")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes

    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)
}