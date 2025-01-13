// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("com.fasterxml.jackson.core:jackson-databind")
    api(project(":common:identifiers"))
    implementation(project(":common:datatypes"))
    api(project(":common:spring-webmvc"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-adapter-input-rest-spring-mvc"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation("org.springframework:spring-web")
    testFixturesApi(project(":common:identifiers"))
    testFixturesApi("com.fasterxml.jackson.core:jackson-databind")
    testFixturesApi("org.springframework:spring-context")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("io.kotest:kotest-assertions-core")
                implementation(project(":common:serialization"))
                implementation(project(":content-types:content-types-adapter-output-simcomp"))
                implementation(testFixtures(project(":common:spring-webmvc")))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
            }
        }
    }
}
