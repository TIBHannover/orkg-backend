@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.spring-restdocs-producer")
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common:exceptions"))
    implementation(project(":graph:graph-application"))

    implementation("org.springframework:spring-context")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-web")

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:kotest")))
                implementation(testFixtures(project(":graph:graph-application")))
                implementation(libs.spring.mockk)
                implementation(libs.kotest.framework.datatest)

                implementation(testFixtures(project(":testing:spring")))
                implementation(project(":graph:graph-adapter-input-rest-spring-mvc")) // because of ExceptionHandler
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
