plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.5.8"))
    // Kotlin
    // TODO: We need 2.0 features, but do not necessarily need the latest Kotlin version.
    //       Once Spring Boot comes with 2.0, we should use this version, unless there is a good reason.
    //       The entries below (kotlin, kotlinx) should be deleted, and the line in the root plugin should be uncommented.
    api(platform(kotlin("bom", "2.2.10")))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.2"))
    // Platforms not included in Spring Boot
    api(platform("dev.forkhandles:forkhandles-bom:2.24.0.0"))
    api(platform("io.kotest:kotest-bom:6.0.7"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:5.2.1"))
    api(platform("org.testcontainers:testcontainers-bom:2.0.2")) // TODO: remove once spring includes tescontainers 2.0.2+ in their BOM

    // Third-party versions not provided by Spring, and without platform/BOM
    api("io.mockk:mockk:1.13.17") // anchor for MockKVirtualPlatformAlignmentRule

    api(platform("com.fasterxml.jackson:jackson-bom:2.20.1"))

    // The Lucene Query Parser version should match the one used by the currently used Neo4j database.
    // Although newer versions should not be a problem, differences in the syntax can be a problem.
    // Check https://mvnrepository.com/artifact/org.neo4j/neo4j-lucene-index/{NEO4J_VERSION}, section "Compile Dependencies",
    // for the version that is used in the respective version, and check the compatibility.
    api("org.apache.lucene:lucene-queryparser:10.1.0")

    val keycloakAdminClientVersion = "26.0.7"
    api("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

    val neo4jMigrationsVersion = "2.20.1"
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:$neo4jMigrationsVersion")
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure:$neo4jMigrationsVersion")

    // JWT
    api("io.jsonwebtoken:jjwt-api:0.13.0")
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Testcontainers
    api("com.github.dasniko:testcontainers-keycloak:3.9.1")

    api("com.redfin:contractual:3.0.0")
    api("org.jbibtex:jbibtex:1.0.20")
    api("net.datafaker:datafaker:2.5.3")
    api("io.hypersistence:hypersistence-utils-hibernate-63:3.13.2") // jsonb support for hibernate
    api("net.handle:handle-client:9.3.2")
    api("org.freemarker:freemarker:2.3.34")
    api("org.apache.commons:commons-csv:1.14.1")
    api("com.github.multiformats:java-multihash:1.3.6")

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")
    }
}
