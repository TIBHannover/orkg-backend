plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-web")
    api("dev.forkhandles:values4k")
    api(project(":common"))

    testFixturesApi("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("dev.forkhandles:fabrikate4k")
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
                implementation("org.assertj:assertj-core")
                implementation("com.redfin:contractual")
            }
        }
    }
}
