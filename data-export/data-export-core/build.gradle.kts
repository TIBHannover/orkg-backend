@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.eclipse.rdf4j:rdf4j-model-api")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api(project(":common:core-identifiers"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":data-export:data-export-ports-input"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation("org.eclipse.rdf4j:rdf4j-model")
    implementation("org.eclipse.rdf4j:rdf4j-model-vocabulary")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.data:spring-data-commons")
    implementation(project(":common:pagination"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":integrations:datacite-serialization"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesImplementation("io.kotest:kotest-assertions-core")
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
    testFixturesImplementation("io.kotest:kotest-runner-junit5")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":data-export:data-export-core")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
