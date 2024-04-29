// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(project(":common"))
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":statistics:statistics-core-model")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("com.jayway.jsonpath:json-path")
            }
        }
    }
}

dependencies {
    api("io.micrometer:micrometer-core")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":statistics:statistics-core-model"))
    api(project(":statistics:statistics-ports-input"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
