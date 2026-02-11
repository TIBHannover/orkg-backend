plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.springframework.batch:spring-batch-core")
    api("org.springframework:spring-web")
    api(project(":common:core-identifiers"))
    api(project(":common:functional"))
    api(project(":common:spring-webmvc"))
    api(project(":content-types:content-types-core-model"))
    api(project(":graph:graph-core-model"))
    implementation("dev.forkhandles:values4k")
    implementation(project(":common:external-identifiers"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesApi("dev.forkhandles:fabrikate4k")
    testFixturesApi("org.springframework.batch:spring-batch-core")
    testFixturesApi(project(":common:core-identifiers"))
    testFixturesImplementation(project(":graph:graph-core-constants"))
    testFixturesImplementation(testFixtures(project(":common:testing")))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-runner-junit5")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation(project(":common:serialization"))
                implementation(testFixtures(project(":common:serialization")))
            }
        }
    }
}
