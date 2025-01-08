// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("java-test-fixtures")
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("jakarta.validation:jakarta.validation-api")
    api(project(":common:identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":feature-flags:feature-flags-ports"))
    api(project(":graph:graph-adapter-input-rest-spring-mvc")) // for representation adapters, TODO: break dependency
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation("org.slf4j:slf4j-api")
    implementation("dev.forkhandles:values4k")
    implementation(project(":common:serialization"))
    api(project(":common:datatypes"))
    implementation(project(":common:functional"))
    testFixturesImplementation(project(":common:datatypes"))
    testFixturesImplementation(project(":common:identifiers"))
    testFixturesImplementation(project(":graph:graph-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                runtimeOnly("org.springframework.boot:spring-boot-starter-security")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-assertions-core")
                implementation("com.ninja-squad:springmockk")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
