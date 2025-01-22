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
    api("jakarta.validation:jakarta.validation-api")
    implementation(project(":common:pagination"))
    api(project(":common:identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-ports-input"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation(project(":community:community-core-model"))

    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesImplementation(testFixtures(project(":graph:graph-core-model")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))

                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("org.assertj:assertj-core")
                implementation("io.kotest:kotest-assertions-core")
                implementation("com.ninja-squad:springmockk")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
            }
        }
    }
}
