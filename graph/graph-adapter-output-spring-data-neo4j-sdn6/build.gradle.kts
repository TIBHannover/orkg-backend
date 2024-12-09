// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))

    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-tx")

    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation(project(":common:neo4j-dsl"))
    implementation("org.apache.lucene:lucene-queryparser")

    testFixturesApi(project(":common"))

    testApi("io.kotest:kotest-framework-api")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("org.assertj:assertj-core")
                implementation(project(":graph:graph-core-services"))
                implementation(testFixtures(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":graph:graph-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-assertions-core")
                runtimeOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
                runtimeOnly("io.kotest:kotest-runner-junit5")
                runtimeOnly(project(":migrations:neo4j-migrations"))
            }
        }
    }
}
