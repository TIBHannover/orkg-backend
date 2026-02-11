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
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("com.ninja-squad:springmockk")
                implementation(testFixtures(project(":statistics:statistics-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}

dependencies {
    api("io.micrometer:micrometer-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-web")
    api(project(":statistics:statistics-core-model"))
    api(project(":statistics:statistics-adapter-input-representations"))
    api(project(":statistics:statistics-ports-input"))

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(project(":common:serialization"))
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
}
