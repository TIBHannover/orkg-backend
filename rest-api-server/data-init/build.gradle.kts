// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-core")
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    api(project(":common"))
    api(project(":community:community-core-model"))
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":feature-flags:feature-flags-ports"))
    api(project(":graph:graph-core-services")) // FIXME: check why needed
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    api(project(":identity-management:idm-ports-input")) // FIXME: might be removed
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.datafaker:datafaker")
    implementation("org.neo4j.driver:neo4j-java-driver")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-beans")
    implementation(libs.jakarta.validation)
    implementation(project(":graph:graph-core-model"))
    implementation(project(":media-storage:media-storage-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-starter-security")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(project(":common:serialization"))
                implementation(project(":graph:graph-adapter-output-in-memory"))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
