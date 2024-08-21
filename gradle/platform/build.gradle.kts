// TODO: Work-around for broken dependencies in AsciiDoctor Gradle plugin. Should be fine for versions >= 4.0.2.
buildscript {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // Change group name and version
            substitute(module("com.burgstaller:okhttp-digest")).using(module("io.github.rburgst:okhttp-digest:1.21"))
        }
    }
}

plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    // Extend existing platforms
    api(enforcedPlatform(kotlin("bom", "1.9.22")))
    api(platform("dev.forkhandles:forkhandles-bom:2.0.0.0"))
    // FIXME: The version is forced because the Spring Gradle plugin version was moved ahead to work around an issue
    //        related to rebuilds.
    //        The force may not be needed; it serves as a safe-guard against accidental upgrades, and can be removed
    //        when the version is updated to the same version as the Gradle plugin.
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.13!!"))
    api(enforcedPlatform("org.junit:junit-bom:5.8.2")) // TODO: can be removed after upgrade to Spring Boot 2.7
    api(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4")) // Required for Kotest. TODO: can be removed after upgrade to Spring Boot 2.7
    // Use the virtual platform defined in the alignment rule to manage Jackson versions
    api(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.15.3"))
    api(platform("io.kotest:kotest-bom:5.4.0"))
    api(platform("org.eclipse.rdf4j:rdf4j-bom:3.7.7"))

    // Force certain version upgrades
    api("org.springframework.data:spring-data-commons:2.7.16!!")
    api("org.springframework:spring-test:5.3.22!!")
    api("org.springframework.data:spring-data-neo4j:6.3.16!!")
    api("org.apache.lucene:lucene-queryparser:9.5.0")
    api("com.epages:restdocs-api-spec:0.16.4")
    api("org.springframework.boot:spring-boot-autoconfigure:2.7.8!!") // mis-alignment in tests; required for Neo4j Migrations
    // Related to the Jakarta madness:
    api("jakarta.activation:jakarta.activation-api:2.1.3!!")
    api("jakarta.annotation:jakarta.annotation-api:2.1.0!!")
    api("jakarta.validation:jakarta.validation-api:2.0.2!!")
    api("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1!!")
    api("org.glassfish.jaxb:jaxb-runtime:2.3.9!!")
    api("org.glassfish.jaxb:txw2:2.3.9!!")

    // Version alignment
    api("io.mockk:mockk:1.13.8") // anchor for MockKVirtualPlatformAlignmentRule

    // Declare constraints on all components that need alignment
    constraints {
        api("com.ninja-squad:springmockk:4.0.2")
    }
}
