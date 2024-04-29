plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api(project(":common"))

    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.springframework.data:spring-data-commons")
    api(libs.forkhandles.values4k)
    implementation("org.apache.lucene:lucene-queryparser") // Search string parsing
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.databind) // for JsonProperty
    // Search string parsing
    implementation("org.apache.lucene:lucene-queryparser:9.5.0")

    api(libs.forkhandles.values4k)

    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi(libs.forkhandles.fabrikate4k)
    testFixturesApi(project(":common"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesRuntimeOnly("org.springframework:spring-core")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":graph:graph-core-model"))

                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(libs.assertj.core)
                implementation(libs.contractual)
            }
        }
    }
}
