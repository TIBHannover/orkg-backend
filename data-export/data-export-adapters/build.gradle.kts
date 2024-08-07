plugins {
    id("org.orkg.gradle.input-adapter-spring-web")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.hamcrest:hamcrest")
                implementation("org.eclipse.rdf4j:rdf4j-util")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.restdocs:spring-restdocs-core")
                implementation("org.springframework:spring-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(project(":data-export:data-export-core"))
            }
        }
    }
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api(project(":common"))
    api(project(":data-export:data-export-ports-input"))
    api(project(":feature-flags:feature-flags-ports"))
    api(project(":graph:graph-adapter-input-rest-spring-mvc")) // for representation adapters, TODO: break dependency
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    api(project(":graph:graph-ports-output"))
    implementation("org.eclipse.rdf4j:rdf4j-model-api")
    implementation("org.eclipse.rdf4j:rdf4j-rio-api")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-beans")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-n3")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-rdfxml")
    runtimeOnly("org.eclipse.rdf4j:rdf4j-rio-trig")

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
