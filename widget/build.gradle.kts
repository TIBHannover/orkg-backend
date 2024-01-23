@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":common:serialization"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))

    implementation("org.springframework:spring-context")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-web")

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:kotest")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(libs.kotest.runner)
                implementation(libs.spring.mockk)
                implementation(libs.kotest.framework.datatest)

                implementation(testFixtures(project(":testing:spring")))
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes
                implementation("org.springframework.boot:spring-boot-starter-web")
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.restdocs)
            }
        }
    }
}
