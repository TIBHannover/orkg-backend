// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.spring-restdocs-producer")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":common:serialization"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
    implementation(project(":community:community-core-model"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":content-types:content-types-ports-input"))
    implementation(project(":content-types:content-types-ports-output"))
    implementation(project(":feature-flags:feature-flags-ports"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.forkhandles.values4k)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testFixturesImplementation(project(":common"))
    testFixturesImplementation(project(":graph:graph-core-model"))
    testFixturesImplementation(project(":community:community-core-model"))
    testFixturesImplementation(project(":content-types:content-types-core-model"))
    testFixturesImplementation(project(":content-types:content-types-core-services"))
    testFixturesImplementation(project(":content-types:content-types-ports-input"))
    testFixturesImplementation(project(":content-types:content-types-ports-output"))
    testFixturesImplementation(libs.forkhandles.values4k)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(project(":community:community-ports-input"))
                implementation(project(":identity-management:idm-ports-input"))
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-security")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.assertj:assertj-core")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
            }
        }
    }
}
