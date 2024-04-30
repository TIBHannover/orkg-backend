// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    api(project(":common"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-core-model"))
    implementation("org.springframework:spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-test")
                implementation(libs.kotest.runner)
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
            }
        }
    }
}
