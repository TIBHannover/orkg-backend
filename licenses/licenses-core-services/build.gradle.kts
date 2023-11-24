plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":licenses:licenses-ports-input"))
    implementation(project(":licenses:licenses-core-model"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.databind)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.spring.mockk)
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
