plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    implementation(project(":common"))

    implementation(libs.jackson.core) // TODO
    implementation("org.springframework:spring-core") // Spring MimeType
    implementation(libs.javax.activation) // Javax Mimetype
    implementation("org.springframework:spring-web")
    implementation("org.springframework.data:spring-data-commons")
    implementation(libs.jackson.databind) // for JsonProperty
    // Search string parsing
    implementation("org.apache.lucene:lucene-queryparser:9.5.0")
    implementation(libs.forkhandles.values4k)

    testFixturesApi(libs.kotest.runner) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testFixturesImplementation(project(":common"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesImplementation("org.springframework:spring-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons") // TODO: does not work?
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa") // org.springframework.data.domain.Page
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(libs.javax.activation) // Javax Mimetype
    testFixturesImplementation(libs.spring.restdocs)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.assertj.core)
                implementation(libs.contractual)
            }
        }
    }
}
