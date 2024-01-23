// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api(project(":common"))
    api(project(":community:community-ports-input"))
    api(project(":content-types:content-types-core-model"))
    api(project(":feature-flags:feature-flags-ports"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output")) // for FormattedLabelRepository

    implementation(project(":community:community-core-model"))

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.security:spring-security-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(libs.jackson.databind)
    api(libs.jakarta.validation)
    api(project(":graph:graph-core-model"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":common:serialization"))
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
                implementation(libs.assertj.core)
                implementation(libs.forkhandles.fabrikate4k)
                implementation(libs.kotest.assertions.core)
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}
