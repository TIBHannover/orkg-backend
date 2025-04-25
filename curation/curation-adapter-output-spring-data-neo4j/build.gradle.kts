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
                implementation("io.kotest:kotest-framework-api")
                implementation("org.springframework:spring-beans")
                implementation("io.kotest:kotest-runner-junit5")
                runtimeOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
                implementation(project(":common:neo4j-dsl"))
                implementation(project(":graph:graph-ports-output"))
                implementation(testFixtures(project(":curation:curation-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly("io.kotest.extensions:kotest-extensions-spring")
                runtimeOnly("io.kotest:kotest-runner-junit5")
                runtimeOnly(project(":migrations:neo4j-migrations"))
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
    api(project(":curation:curation-ports-output"))
    api(project(":graph:graph-adapter-output-spring-data-neo4j"))
    api(project(":graph:graph-core-model"))
    implementation(project(":common:pagination"))
}
