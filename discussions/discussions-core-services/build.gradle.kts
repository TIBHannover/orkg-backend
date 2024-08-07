plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api(project(":common"))
    api(project(":community:community-ports-output"))
    api(project(":discussions:discussions-core-model"))
    api(project(":discussions:discussions-ports-input"))
    api(project(":discussions:discussions-ports-output"))
    api(project(":graph:graph-ports-output"))
    implementation(project(":community:community-core-model"))
    implementation(project(":graph:graph-core-model"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.eclipse.rdf4j:rdf4j-util")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(testFixtures(project(":graph:graph-core-model")))
            }
        }
    }
}
