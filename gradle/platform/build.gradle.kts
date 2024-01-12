plugins {
    id("org.orkg.gradle.platform")
}

dependencies {
    // Extend existing platforms
    api(enforcedPlatform(kotlin("bom", "1.8.22")))
    api(platform("dev.forkhandles:forkhandles-bom:2.0.0.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.13"))
    api(enforcedPlatform("org.junit:junit-bom:5.8.2")) // TODO: can be removed after upgrade to Spring Boot 2.7
    api(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4")) // Required for Kotest. TODO: can be removed after upgrade to Spring Boot 2.7
    // Use the virtual platform defined in the alignment rule to manage Jackson versions
    api(enforcedPlatform("com.fasterxml.jackson:jackson-bom:2.15.3"))

    // Force certain version upgrades
    api("org.springframework.data:spring-data-commons:2.7.16!!")
    api("org.springframework.data:spring-data-neo4j:6.3.16!!")
    // Related to capability resolution:
    api("com.sun.activation:jakarta.activation:1.2.2!!")

    // Declare constraints on all components that need alignment
    constraints {
    }
}
