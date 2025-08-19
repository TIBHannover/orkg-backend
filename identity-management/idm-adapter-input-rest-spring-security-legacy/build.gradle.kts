// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":common:spring-webmvc")))
                implementation(testFixtures(project(":testing:spring")))
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-assertions-core")
                runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework.security:spring-security-oauth2-resource-server")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-web")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-output"))
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-oauth2-core")
    implementation("org.springframework.security:spring-security-oauth2-jose")
}
