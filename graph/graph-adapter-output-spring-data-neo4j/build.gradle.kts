// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api("org.eclipse.rdf4j:rdf4j-common-io")
    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    api("org.springframework.boot:spring-boot-persistence")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api(project(":common:core-identifiers"))
    api(project(":common:spring-data"))
    api(project(":common:neo4j-dsl"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    implementation("org.apache.lucene:lucene-queryparser")
    implementation(project(":common:datatypes"))
    implementation(project(":common:spring-webmvc"))
    implementation(project(":common:string-utils"))
    implementation(project(":graph:graph-core-constants"))

    testFixturesApi(project(":common:core-identifiers"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j"))
                implementation(testFixtures(project(":common:testing")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation("io.kotest:kotest-assertions-core")
                implementation("io.kotest:kotest-framework-engine")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("org.assertj:assertj-core")
                implementation(project(":graph:graph-core-services"))
                implementation(testFixtures(project(":graph:graph-adapter-output-spring-data-neo4j")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":graph:graph-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                runtimeOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
                runtimeOnly("io.kotest:kotest-extensions-spring")
                runtimeOnly("io.kotest:kotest-runner-junit5")
                runtimeOnly(project(":migrations:neo4j-migrations"))
            }
        }
    }
}
