plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(libs.forkhandles.fabrikate4k)
            }
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":licenses:licenses-ports-input"))
    implementation(project(":licenses:licenses-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
