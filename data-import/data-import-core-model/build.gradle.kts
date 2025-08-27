plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.batch:spring-batch-core")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesImplementation(project(":common:core-identifiers"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:serialization")))
            }
        }
    }
}
