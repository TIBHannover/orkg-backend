plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:4.0.2"))
    // Kotlin
    api(platform(kotlin("bom", "2.2.20")))
    // Platforms not included in Spring Boot
    api(platform("dev.forkhandles:forkhandles-bom:2.24.0.0"))
    api(platform("io.kotest:kotest-bom:6.0.7"))
    api(platform("io.rest-assured:rest-assured-bom:6.0.0"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:5.2.2"))

//    api(platform("tools.jackson:jackson-bom:3.0.3"))

    // Upgrade to Hibernate 7.3 for Jackson 3 support. Can be removed once Spring includes Hibernate 7.3+.
    api("org.hibernate.orm:hibernate-core:7.3.0.CR1")

    // Third-party versions not provided by Spring, and without platform/BOM
    api("io.mockk:mockk:1.14.7") // anchor for MockKVirtualPlatformAlignmentRule

    // The Lucene Query Parser version should match the one used by the currently used Neo4j database.
    // Although newer versions should not be a problem, differences in the syntax can be a problem.
    // Check https://mvnrepository.com/artifact/org.neo4j/neo4j-lucene-index/{NEO4J_VERSION}, section "Compile Dependencies",
    // for the version that is used in the respective version, and check the compatibility.
    api("org.apache.lucene:lucene-queryparser:10.1.0")

    val keycloakAdminClientVersion = "26.0.7"
    api("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

    val neo4jMigrationsVersion = "3.1.0"
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:$neo4jMigrationsVersion")
    api("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure:$neo4jMigrationsVersion")

    // JWT
    api("io.jsonwebtoken:jjwt-api:0.13.0")
    api("io.jsonwebtoken:jjwt-impl:0.13.0")
    api("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // Testcontainers
    api("com.github.dasniko:testcontainers-keycloak:4.0.1")

    api("com.redfin:contractual:3.0.0")
    api("org.jbibtex:jbibtex:1.0.20")
    api("net.datafaker:datafaker:2.5.3")
    api("io.hypersistence:hypersistence-utils-hibernate-73:3.15.1") // JSONB support for Hibernate
    api("net.handle:handle-client:9.3.2")
    api("org.freemarker:freemarker:2.3.34")
    api("org.apache.commons:commons-csv:1.14.1")
    api("com.github.multiformats:java-multihash:1.3.6")

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:5.0.1")
    }
}
