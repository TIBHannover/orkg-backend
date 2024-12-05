plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    // Extend existing platforms
    api(platform(kotlin("bom", "2.0.20")))
    api(platform("dev.forkhandles:forkhandles-bom:2.0.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.0"))
    api(platform("io.kotest:kotest-bom:5.9.1"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:5.0.3"))

    // Third-party versions not provided by Spring, and without platform/BOM
    api("org.apache.lucene:lucene-queryparser:9.5.0") // Needs to match Neo4j database version
    api("io.mockk:mockk:1.13.13") // anchor for MockKVirtualPlatformAlignmentRule

    // Restdocs API Spec (OpenAPI)
    val apiSpecVersion = "0.19.4"
    api("com.epages:restdocs-api-spec:$apiSpecVersion") // also acts as anchor for RestdocsApiSpecVirtualPlatformAlignmentRule
    api("com.epages:restdocs-api-spec-mockmvc:$apiSpecVersion")

    val keycloakAdminClientVersion = "26.0.2"
    api("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

    val neo4jMigrationsVersion = "2.13.4"
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:$neo4jMigrationsVersion")
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure:$neo4jMigrationsVersion")

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")
    }
}
