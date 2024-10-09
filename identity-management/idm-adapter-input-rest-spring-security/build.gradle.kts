// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.mockito:mockito-core")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.security:spring-security-core")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation(libs.jackson.databind)
                runtimeOnly(libs.jackson.kotlin)
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework:spring-web")
    api("jakarta.validation:jakarta.validation-api")
    api(project(":identity-management:idm-core-model"))
    api(project(":identity-management:idm-ports-input"))
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(project(":common")) // for exceptions

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
