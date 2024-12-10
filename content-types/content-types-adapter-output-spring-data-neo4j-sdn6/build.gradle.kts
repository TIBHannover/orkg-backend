// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib") // "downgrade" from api()
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-data-neo4j")
                implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
                implementation("io.kotest:kotest-framework-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation("io.kotest:kotest-runner-junit5")
                runtimeOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
                implementation(project(":graph:graph-core-services"))
                runtimeOnly(project(":migrations:neo4j-migrations"))
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("org.springframework.data:spring-data-neo4j")
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
            }
        }
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api(project(":common:identifiers"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    implementation(project(":common:neo4j-dsl"))
    implementation("dev.forkhandles:values4k")
}
