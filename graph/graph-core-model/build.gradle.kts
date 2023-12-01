plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.jackson-conventions")
    `java-test-fixtures`
    alias(libs.plugins.spotless)
    id("org.orkg.neo4j-conventions") // to obtain version of spring-data commons. TODO: remove after upgrade
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common"))
    implementation(project(":identity-management:idm-core-model"))
    implementation(project(":identity-management:idm-core-services"))
    // FIXME: break dependency; for User

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
    "testFixturesImplementation"(project(":graph:graph-core-services")) // only Asciidoc helpers
    testFixturesImplementation("org.springframework:spring-core")
    testFixturesImplementation("org.springframework.data:spring-data-commons") // TODO: does not work?
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa") // org.springframework.data.domain.Page
    testFixturesImplementation(libs.forkhandles.fabrikate4k)
    testFixturesImplementation(libs.forkhandles.values4k)
    testFixturesImplementation(libs.javax.activation) // Javax Mimetype
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                //implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(libs.assertj.core)
                implementation(libs.contractual)
            }
        }
    }
}