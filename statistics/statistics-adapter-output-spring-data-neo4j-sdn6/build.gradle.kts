// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.gradle.spring-library")
    id("org.orkg.gradle.kotlin-library-with-container-tests")
}

val neo4jMigrations: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

testing {
    suites {
        val containerTest by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(testFixtures(project(":testing:spring")))
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")) // for SDN adapters, TODO: refactor?
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-ports-output"))
                implementation(project(":migrations:neo4j-migrations"))
                implementation(testFixtures(project(":statistics:statistics-ports-output")))

                implementation(libs.kotest.extensions.spring)
                implementation(libs.spring.boot.starter.neo4j.migrations)

                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
                    exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
                }
                implementation("org.springframework.data:spring-data-neo4j:6.3.16")
                implementation(libs.kotest.runner)
            }
        }
    }
}

dependencies {
    implementation(project(":statistics:statistics-ports-output"))

    implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")) // for Neo4jLiteral, TODO: break dependency

    implementation(project(":common"))
    implementation(project(":common:neo4j-dsl"))

    // Pagination (e.g. Page, Pageable, etc.)
    implementation("org.springframework.data:spring-data-commons")

    // Forkhandles
    implementation(libs.forkhandles.values4k)

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
    }
    // implementation("org.neo4j:neo4j-cypher-dsl:2022.8.5")
    implementation("org.springframework.data:spring-data-neo4j")

    neo4jMigrations(project(mapOf("path" to ":migrations:neo4j-migrations", "configuration" to "neo4jMigrations")))
}
