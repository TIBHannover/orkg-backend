plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common:exceptions"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
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
