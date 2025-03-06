@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation(project(":common:pagination"))
    implementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation(project(":common:serialization"))
                runtimeOnly("com.jayway.jsonpath:json-path")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
            }
        }
    }
}
