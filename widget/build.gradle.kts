@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":common:identifiers"))
    implementation(project(":common:spring-webmvc"))
    api(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("com.epages:restdocs-api-spec")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-framework-datatest")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("com.ninja-squad:springmockk")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes
            }
        }
    }
}
