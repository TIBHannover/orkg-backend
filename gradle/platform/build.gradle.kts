plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    // Extend existing platforms
    api(platform(kotlin("bom", "2.1.10")))
    api(platform("dev.forkhandles:forkhandles-bom:2.20.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.2"))
    api(platform("io.kotest:kotest-bom:5.9.1"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:5.1.1"))
    api(platform("org.assertj:assertj-bom:3.27.3"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.1"))

    // Third-party versions not provided by Spring, and without platform/BOM
    api("io.mockk:mockk:1.13.16") // anchor for MockKVirtualPlatformAlignmentRule

    // The Lucene Query Parser version should match the one used by the currently used Neo4j database.
    // Although newer versions should not be a problem, differences in the syntax can be a problem.
    // Check https://mvnrepository.com/artifact/org.neo4j/neo4j-lucene-index/{NEO4J_VERSION}, section "Compile Dependencies",
    // for the version that is used in the respective version, and check the compatibility.
    api("org.apache.lucene:lucene-queryparser:10.1.0")

    // Restdocs API Spec (OpenAPI)
    val apiSpecVersion = "0.19.4"
    api("com.epages:restdocs-api-spec:$apiSpecVersion") // also acts as anchor for RestdocsApiSpecVirtualPlatformAlignmentRule
    api("com.epages:restdocs-api-spec-mockmvc:$apiSpecVersion")

    val keycloakAdminClientVersion = "26.0.4"
    api("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

    val neo4jMigrationsVersion = "2.15.2"
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:$neo4jMigrationsVersion")
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure:$neo4jMigrationsVersion")

    // Kotest
    api("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    api("io.kotest.extensions:kotest-extensions-testcontainers:2.0.2")

    // Testcontainers
    api("com.github.dasniko:testcontainers-keycloak:3.6.0")

    api("com.redfin:contractual:3.0.0")
    api("org.jbibtex:jbibtex:1.0.20")
    api("net.datafaker:datafaker:2.4.2")
    api("commons-fileupload:commons-fileupload:1.5")

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")

        // Newer than Spring Boot, no BOM / platform
        api("com.github.ben-manes.caffeine:caffeine:3.2.0")
        api("org.neo4j:neo4j-cypher-dsl:2024.4.0")
    }
}
