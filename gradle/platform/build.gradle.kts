plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    // Extend existing platforms
    api(enforcedPlatform(kotlin("bom", "1.9.22")))
    api(platform("dev.forkhandles:forkhandles-bom:2.0.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.13"))
    api(enforcedPlatform("org.junit:junit-bom:5.8.2")) // TODO: can be removed after upgrade to Spring Boot 2.7
    api(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4")) // Required for Kotest. TODO: can be removed after upgrade to Spring Boot 2.7
    // Use the virtual platform defined in the alignment rule to manage Jackson versions
    api(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.15.3"))
    api(platform("io.kotest:kotest-bom:5.4.0"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:3.7.7"))

    // Force certain version upgrades
    api("org.springframework.data:spring-data-commons:2.7.16!!")
    api("org.springframework.data:spring-data-neo4j:6.3.16!!")
    api("org.apache.lucene:lucene-queryparser:9.5.0")
    api("com.epages:restdocs-api-spec:0.16.4")
    api("org.springframework.boot:spring-boot-autoconfigure:2.7.8!!") // mis-alignment in tests; required for Neo4j Migrations
    // Related to capability resolution:
    api("com.sun.activation:jakarta.activation:1.2.2!!")
    // Version alignment
    api("io.mockk:mockk:1.13.8") // anchor for MockKVirtualPlatformAlignmentRule

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")
    }
}
