// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("jakarta.servlet:jakarta.servlet-api")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-web")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":common:core-identifiers"))
                implementation(project(":common:serialization"))
                implementation(project(":data-import:data-import-core-model"))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}
