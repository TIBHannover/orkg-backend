// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("java-test-fixtures")
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.eclipse.rdf4j:rdf4j-common-io")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework:spring-test")
                implementation("com.ninja-squad:springmockk")
                implementation(project(":common:core-identifiers"))
                implementation(testFixtures(project(":graph:graph-adapter-input-rest-spring-mvc")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-web")
    api(project(":curation:curation-ports-input"))
    api(project(":graph:graph-adapter-input-representations"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(project(":common:serialization"))
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
    testFixturesApi(testFixtures(project(":testing:spring")))
}
