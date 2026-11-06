plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    api(platform(libs.bom.spring.boot))
    // Kotlin
    api(platform(libs.bom.kotlin))
    // Platforms not included in Spring Boot
    api(platform(libs.bom.forkhandles))
    api(platform(libs.bom.kotest))
    api(platform(libs.bom.rest.assured))
    api(platform(libs.bom.rdf4j))
    api(platform(libs.bom.axon))
    // May not be needed when axon bom and spring bom use the same version
    api(platform(libs.bom.junit))

    // Third-party versions not provided by Spring, and without platform/BOM
    api(libs.mockk) // anchor for MockKVirtualPlatformAlignmentRule
    api(libs.commonmark) // anchor for CommonMarkVirtualPlatformAlignmentRule
    api(libs.commonmark.ext.autolink)
    api(libs.commonmark.ext.gfm.strikethrough)
    api(libs.commonmark.ext.gfm.tables)
    api(libs.equalsverifier)

    // The Lucene Query Parser version should match the one used by the currently used Neo4j database.
    // Although newer versions should not be a problem, differences in the syntax can be a problem.
    // Check https://mvnrepository.com/artifact/org.neo4j/neo4j-lucene-index/{NEO4J_VERSION}/dependencies
    // for the respective version, and check the compatibility.
    api(libs.lucene.queryparser)

    api(libs.keycloak.admin.client)

    api(libs.neo4j.migrations.spring.boot.starter)
    api(libs.neo4j.migrations.spring.boot.autoconfigure)

    // JWT
    api(libs.jsonwebtoken.api)
    api(libs.jsonwebtoken.impl)
    api(libs.jsonwebtoken.jackson)

    // Testcontainers
    api(libs.testcontainers.keycloak)

    api(libs.contractual)
    api(libs.jbibtex)
    api(libs.datafaker)
    api(libs.hypersistence.utils.hibernate) // JSONB support for Hibernate
    api(libs.handle.client)
    api(libs.freemarker)
    api(libs.apache.commons.csv)
    api(libs.multiformats.multihash)
    api(libs.kotlinx.html.jvm)

    // Declare constraints on all components that need alignment
    constraints {
        api(libs.spring.mockk)
    }
}
