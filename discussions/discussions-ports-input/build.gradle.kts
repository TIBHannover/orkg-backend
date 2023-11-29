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
    implementation(project(":graph:graph-core-services"))
    implementation(project(":discussions:discussions-core-model"))

    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.core)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
