// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":common")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation(libs.kotest.assertions.core)
                runtimeOnly(libs.jackson.kotlin)
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework.security:spring-security-oauth2-resource-server")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-web")
    api(libs.jackson.databind)
    api(project(":common")) // for exceptions
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-output"))
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-oauth2-core")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
