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
                implementation("org.springframework.security:spring-security-test")
                implementation(libs.spring.mockk)
                implementation(libs.spring.restdocs)
                implementation(libs.forkhandles.fabrikate4k)
            }
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data-export:data-export-core"))
    implementation(project(":data-export:data-export-ports-input"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))
    implementation(project(":graph:graph-adapter-input-rest-spring-mvc")) // for representation adapters, TODO: break dependency
    implementation(project(":feature-flags:feature-flags-ports"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // to (de)serialize data classes

    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }

    testApi(enforcedPlatform(libs.junit5.bom)) // TODO: can be removed after upgrade to Spring Boot 2.7
}
