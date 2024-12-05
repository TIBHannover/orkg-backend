plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-model-api")
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework:spring-beans")
    api("org.springframework:spring-context")
    api(libs.jackson.databind)
    api(project(":common"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":data-export:data-export-ports-input"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("org.eclipse.rdf4j:rdf4j-model")
    implementation("org.eclipse.rdf4j:rdf4j-model-vocabulary")
    implementation("org.springframework.data:spring-data-commons")
    implementation(project(":content-types:content-types-core-model"))
    implementation("org.slf4j:slf4j-api")

    testFixturesImplementation(libs.kotest.runner)
    testFixturesImplementation("io.kotest:kotest-assertions-shared")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-shared")
                implementation("io.kotest:kotest-common")
                implementation("io.kotest:kotest-framework-api")
                implementation("io.kotest:kotest-framework-engine")
                implementation("io.mockk:mockk-dsl")
                implementation("io.mockk:mockk-jvm")
                implementation("org.assertj:assertj-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-params")
                implementation(testFixtures(project(":content-types:content-types-core-model")))
                implementation(testFixtures(project(":data-export:data-export-core")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":testing:spring")))
            }
        }
    }
}
