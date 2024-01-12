import org.gradlex.javaecosystem.capabilities.rules.JakartaActivationApiRule
import org.gradlex.javaecosystem.capabilities.rules.JakartaActivationImplementationRule
import org.orkg.gradle.metadatarules.versionalignment.JacksonBomAlignmentRule

plugins {
    id("dev.jacomet.logging-capabilities")
    id("org.gradlex.java-ecosystem-capabilities")
}

// Configure logging capabilities plugin to default to Logback
loggingCapabilities.enforceLogback()

dependencies.components {
    all<JacksonBomAlignmentRule>()
}

configurations.all {
    resolutionStrategy.capabilitiesResolution {
        // TODO: Re-evaluate after upgrade of Spring Boot. May not be needed anymore.
        withCapability("javax.activation:activation") {
            select("com.sun.activation:jakarta.activation:1.2.2")
        }
    }
    resolutionStrategy.eachDependency {
        // TODO: Re-evaluate after upgrade of Spring Boot. May not be needed anymore.
        if (requested.group == "org.slf4j") {
            useVersion("1.7.36")
            because("we have different versions in the classpath, but this one is used in Spring Boot <= 3.0")
        }
    }
}
