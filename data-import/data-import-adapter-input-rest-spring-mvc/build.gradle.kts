// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("tools.jackson.core:jackson-core")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api("tools.jackson.core:jackson-databind")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":content-types:content-types-adapter-input-representations"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":data-import:data-import-ports-input"))
    api(project(":data-import:data-import-core-model"))
    implementation(project(":common:functional"))

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi(project(":common:serialization"))
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesImplementation(testFixtures(project(":data-import:data-import-core-model")))
    testFixturesImplementation(testFixtures(project(":common:external-identifiers")))
    testFixturesImplementation(testFixtures(project(":content-types:content-types-adapter-input-rest-spring-mvc")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.epages:restdocs-api-spec")
                implementation("com.ninja-squad:springmockk")
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("jakarta.servlet:jakarta.servlet-api")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation(project(":graph:graph-core-constants"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":data-import:data-import-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}
