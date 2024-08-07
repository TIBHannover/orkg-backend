// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.eclipse.rdf4j:rdf4j-util")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(libs.spring.mockk)
                implementation(project(":common"))
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}

dependencies {
    api("org.springframework.data:spring-data-commons:2.7.16")
    api("org.springframework:spring-web")
    api(project(":curation:curation-ports-input"))
    api(project(":graph:graph-adapter-input-rest-spring-mvc"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
