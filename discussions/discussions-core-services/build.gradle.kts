plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))

    api(project(":discussions:discussions-ports-input"))
    api(project(":discussions:discussions-ports-output"))

    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":community:community-ports-output"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(libs.spring.mockk)
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            }
        }
    }
}
