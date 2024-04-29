// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":statistics:statistics-core-model")))

                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(libs.forkhandles.fabrikate4k)
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":statistics:statistics-ports-input"))
    implementation(project(":statistics:statistics-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
