// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

val neo4jMigrations: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    api(project(":common"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))

    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl:2022.8.5")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api("org.springframework:spring-core")
    api("org.springframework:spring-tx")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation(libs.forkhandles.values4k)
    implementation(project(":common:neo4j-dsl"))

    testFixturesApi(project(":common"))

    neo4jMigrations(project(mapOf("path" to ":migrations:neo4j-migrations", "configuration" to "neo4jMigrations")))

    containerTestApi("io.kotest:kotest-framework-api")
    containerTestApi(project(":graph:graph-ports-output"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-test")
                implementation(libs.assertj.core)
            }
        }
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":common"))
                implementation(project(":graph:graph-core-model"))
                implementation(project(":graph:graph-core-services"))
                runtimeOnly(project(":migrations:neo4j-migrations"))
                implementation(testFixtures(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(testFixtures(project(":graph:graph-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.neo4j:neo4j-cypher-dsl")
                implementation("org.springframework.boot:spring-boot-autoconfigure")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation(libs.kotest.assertions.core)
                implementation(libs.spring.boot.starter.neo4j.migrations)
                runtimeOnly(libs.kotest.extensions.spring)
                runtimeOnly(libs.kotest.runner)
            }
        }
    }
}
