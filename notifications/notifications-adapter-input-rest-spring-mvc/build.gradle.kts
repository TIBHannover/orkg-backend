// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("jakarta.validation:jakarta.validation-api")
    api("org.springframework.security:spring-security-oauth2-resource-server")
    api("org.springframework:spring-web")
    api(project(":common:spring-webmvc"))
    api(project(":notifications:notifications-ports-input"))
    implementation("org.springframework.security:spring-security-oauth2-core")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation(project(":notifications:notifications-core-model"))

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                runtimeOnly("org.springframework.boot:spring-boot-starter-security")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("com.ninja-squad:springmockk")
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
