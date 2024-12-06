// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":graph:graph-ports-output"))
    api(project(":graph:graph-core-model"))
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation(project(":common"))
    runtimeOnly("com.fasterxml.jackson.core:jackson-core")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.kotest:kotest-assertions-core")
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
            }
        }
    }
}
