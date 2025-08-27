// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":content-types:content-types-adapter-input-representations"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":data-import:data-import-ports-input"))
    api(project(":data-import:data-import-core-model"))
    implementation(project(":common:functional"))

    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesImplementation(testFixtures(project(":data-import:data-import-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.ninja-squad:springmockk")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("jakarta.servlet:jakarta.servlet-api")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":content-types:content-types-adapter-input-rest-spring-mvc")))
                implementation(testFixtures(project(":data-import:data-import-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}
