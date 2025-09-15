plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.4.3"))
    // Kotlin
    // TODO: We need 2.0 features, but do not necessarily need the latest Kotlin version.
    //       Once Spring Boot comes with 2.0, we should use this version, unless there is a good reason.
    //       The entries below (kotlin, kotlinx) should be deleted, and the line in the root plugin should be uncommented.
    api(platform(kotlin("bom", "2.2.10")))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.1"))
    // Platforms not included in Spring Boot
    api(platform("dev.forkhandles:forkhandles-bom:2.22.2.1"))
    api(platform("io.kotest:kotest-bom:6.0.2"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:5.1.2"))

    // Third-party versions not provided by Spring, and without platform/BOM
    api("io.mockk:mockk:1.13.17") // anchor for MockKVirtualPlatformAlignmentRule

    // The Lucene Query Parser version should match the one used by the currently used Neo4j database.
    // Although newer versions should not be a problem, differences in the syntax can be a problem.
    // Check https://mvnrepository.com/artifact/org.neo4j/neo4j-lucene-index/{NEO4J_VERSION}, section "Compile Dependencies",
    // for the version that is used in the respective version, and check the compatibility.
    api("org.apache.lucene:lucene-queryparser:10.1.0")

    // Restdocs API Spec (OpenAPI)
    val apiSpecVersion = "0.19.4"
    api("com.epages:restdocs-api-spec:$apiSpecVersion") // also acts as anchor for RestdocsApiSpecVirtualPlatformAlignmentRule
    api("com.epages:restdocs-api-spec-mockmvc:$apiSpecVersion")

    val keycloakAdminClientVersion = "26.0.5"
    api("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

    val neo4jMigrationsVersion = "2.17.0"
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:$neo4jMigrationsVersion")
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure:$neo4jMigrationsVersion")

    // Testcontainers
    api("com.github.dasniko:testcontainers-keycloak:3.6.0")

    api("com.redfin:contractual:3.0.0")
    api("org.jbibtex:jbibtex:1.0.20")
    api("net.datafaker:datafaker:2.4.2")
    api("commons-fileupload:commons-fileupload:1.5")
    api("io.hypersistence:hypersistence-utils-hibernate-63:3.9.9") // jsonb support for hibernate
    api("net.handle:handle-client:9.3.1")
    api("org.freemarker:freemarker:2.3.34")
    api("org.apache.commons:commons-csv:1.14.1")

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")
    }
}
