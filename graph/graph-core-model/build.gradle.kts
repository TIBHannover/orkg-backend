plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("dev.forkhandles:values4k")
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    implementation(project(":common:datatypes"))
    implementation(project(":common:string-utils"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesApi("org.eclipse.rdf4j:rdf4j-common-io")
    testFixturesApi("org.springframework.restdocs:spring-restdocs-core")
    testFixturesApi("dev.forkhandles:fabrikate4k")
    testFixturesApi(project(":common:core-identifiers"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(testFixtures(project(":testing:spring")))
    testFixturesRuntimeOnly("org.springframework:spring-core")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("com.redfin:contractual")
                implementation("io.kotest:kotest-assertions-core")
                implementation("org.assertj:assertj-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation("org.junit.platform:junit-platform-commons")
                implementation("org.opentest4j:opentest4j")
                implementation(project(":graph:graph-core-model"))
            }
        }
    }
}
