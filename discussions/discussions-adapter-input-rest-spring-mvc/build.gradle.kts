// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("com.ninja-squad:springmockk")
                implementation(project(":common:serialization"))
                implementation(project(":community:community-core-model"))
                implementation(project(":graph:graph-core-model"))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("jakarta.validation:jakarta.validation-api")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":common"))
    api(project(":discussions:discussions-core-model"))
    api(project(":discussions:discussions-ports-input"))
}
