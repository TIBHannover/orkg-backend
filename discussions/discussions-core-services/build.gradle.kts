plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":discussions:discussions-core-model"))
    implementation(project(":discussions:discussions-ports-input"))
    implementation(project(":discussions:discussions-ports-output"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":identity-management:idm-ports-input"))
    implementation(project(":identity-management:idm-ports-output"))

    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":identity-management:idm-core-model")))
                implementation(libs.spring.mockk)
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
            }
        }
    }
}
