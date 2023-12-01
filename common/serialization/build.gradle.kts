plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))

    implementation("org.springframework:spring-context")
    implementation(libs.jackson.databind)
    implementation(libs.jackson.kotlin)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // exclude(module = "mockito-core") // TODO: uncomment when migrated to MockK
                }
                implementation(libs.kotest.runner)
            }
        }
    }
}