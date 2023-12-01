// JVM Test Suite is still incubating, but expected to be stable soon, so disabling the warning.
@file:Suppress("UnstableApiUsage")

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    //kotlin("jvm") // TODO: remove on upgrade
    alias(libs.plugins.spring.boot) apply false
    kotlin("plugin.spring")
    alias(libs.plugins.spotless)
}

val neo4jMigrations: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
        val containerTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.FUNCTIONAL_TEST)
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(project(":graph:graph-core-model"))
                implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-ports-output"))
                implementation(project(":migrations:liquibase"))
                implementation(project(":migrations:neo4j-migrations"))
                implementation(testFixtures(project(":content-types:content-types-ports-output")))

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
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

dependencies {
    api(platform(project(":platform")))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))
    implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))

    implementation(project(":common"))
    implementation(project(":common:neo4j-dsl"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":content-types:content-types-ports-output"))

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

    // Caching
    implementation("org.springframework.boot:spring-boot-starter-cache")

    neo4jMigrations(project(mapOf("path" to ":migrations:neo4j-migrations", "configuration" to "neo4jMigrations")))
}

tasks.named("check") {
    dependsOn(testing.suites.named("containerTest"))
}