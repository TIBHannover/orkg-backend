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
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    exclude(module = "mockito-core")
                }
                implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
                    exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
                }
                implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
                implementation("io.kotest:kotest-framework-api")
                implementation("org.springframework.boot:spring-boot-autoconfigure")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework.data:spring-data-neo4j:6.3.16")
                implementation("org.springframework:spring-beans")
                implementation("org.springframework:spring-context")
                implementation("org.springframework:spring-test")
                implementation(libs.kotest.runner)
                implementation(libs.spring.boot.starter.neo4j.migrations)
                implementation(project(":content-types:content-types-ports-output"))
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")) // for SDN adapters, TODO: refactor?
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-ports-output"))
                implementation(project(":migrations:neo4j-migrations"))
                implementation(project())
                implementation(testFixtures(project(":content-types:content-types-ports-output")))
                implementation(testFixtures(project(":testing:spring")))
                runtimeOnly(libs.kotest.extensions.spring)
            }
        }
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework.data:spring-data-commons")
    api("org.springframework.data:spring-data-neo4j")
    api("org.springframework:spring-context")
    api(project(":common"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-output"))
    api(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6")) // for Neo4jLiteral, TODO: break dependency
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-output"))
    api("org.neo4j.driver:neo4j-java-driver")
    api("org.neo4j:neo4j-cypher-dsl")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation(project(":common:neo4j-dsl"))

    containerTestImplementation(kotlin("stdlib")) // "downgrade" from api()

    neo4jMigrations(project(mapOf("path" to ":migrations:neo4j-migrations", "configuration" to "neo4jMigrations")))
}
