plugins {
    id("org.orkg.kotlin-conventions")
    id("java-test-fixtures")
    id("org.orkg.jackson-conventions")
    kotlin("plugin.spring")
    id("org.orkg.neo4j-conventions")
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":common"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))

    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":content-types:content-types-ports-input"))
    implementation(project(":data-export:data-export-ports-input"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }

    testFixturesImplementation(libs.kotest.runner)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":data-export:data-export-core")))
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(libs.spring.mockk)
                implementation("org.assertj:assertj-core")
            }
        }
    }
}
