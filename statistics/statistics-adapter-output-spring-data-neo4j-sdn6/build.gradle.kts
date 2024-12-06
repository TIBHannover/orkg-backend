// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
                implementation("io.kotest:kotest-framework-api")
                implementation("org.springframework.boot:spring-boot-autoconfigure")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-runner-junit5")
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")) // for SDN adapters, TODO: refactor?
                implementation(project(":graph:graph-ports-output"))
                runtimeOnly(project(":migrations:neo4j-migrations"))
                implementation(project(":statistics:statistics-ports-output"))
                implementation(project())
                implementation(testFixtures(project(":statistics:statistics-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
                runtimeOnly("io.kotest:kotest-runner-junit5")
            }
        }
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api(project(":statistics:statistics-ports-output"))
    implementation("org.neo4j:neo4j-cypher-dsl")
    implementation(project(":common:neo4j-dsl"))

    containerTestImplementation(kotlin("stdlib")) // "downgrade" from api()
}
