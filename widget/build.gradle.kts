@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":graph:graph-ports-input"))
    implementation(project(":common:spring-webmvc"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(project(":common:serialization"))
    testFixturesApi(testFixtures(project(":testing:spring")))
    testFixturesApi(testFixtures(project(":common:core-identifiers")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.ninja-squad:springmockk")
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-webmvc-test")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes
            }
        }
    }
}
