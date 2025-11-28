plugins {
    id("java-test-fixtures")
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("com.epages:restdocs-api-spec")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.eclipse.rdf4j:rdf4j-common-io")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("com.ninja-squad:springmockk")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation(project(":data-export:data-export-core"))
            }
        }
    }
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":data-export:data-export-ports-input"))
    api(project(":graph:graph-adapter-input-representations"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation("org.eclipse.rdf4j:rdf4j-model-api")
    implementation("org.eclipse.rdf4j:rdf4j-rio-api")
    implementation("org.slf4j:slf4j-api")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-jsonld")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-n3")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-ntriples")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-nquads")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-rdfxml")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-trig")

    testFixturesApi("org.springframework:spring-context")
    testFixturesApi("org.springframework.boot:spring-boot-test")
    testFixturesApi(project(":common:spring-webmvc"))
    testFixturesApi(testFixtures(project(":testing:spring")))
}
