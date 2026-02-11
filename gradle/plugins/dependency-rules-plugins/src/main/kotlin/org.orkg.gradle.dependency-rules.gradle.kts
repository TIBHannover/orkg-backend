import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition
import org.orkg.gradle.metadatarules.versionalignment.JacksonBomAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.MockKVirtualPlatformAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.RestdocsApiSpecVirtualPlatformAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.TestcontainersBomAlignmentRule

plugins {
    id("dev.jacomet.logging-capabilities")
    id("org.gradlex.jvm-dependency-conflict-resolution")
}

// Configure logging capabilities plugin to default to Logback
loggingCapabilities.enforceLogback()

jvmDependencyConflicts {
    consistentResolution {
        platform("org.orkg:platform")
    }
    conflictResolution {
        select(CapabilityDefinition.JAKARTA_SERVLET_API, "org.apache.tomcat.embed:tomcat-embed-core")
        select(CapabilityDefinition.JAKARTA_ACTIVATION_API, "jakarta.activation:jakarta.activation-api")
        select(CapabilityDefinition.JAKARTA_ACTIVATION_IMPL, "org.eclipse.angus:angus-activation")
        select(CapabilityDefinition.SLF4J_IMPL, "ch.qos.logback:logback-classic")
    }
}

dependencies.components {
//    all<JacksonBomAlignmentRule>()
    all<MockKVirtualPlatformAlignmentRule>()
    all<RestdocsApiSpecVirtualPlatformAlignmentRule>()
//    all<TestcontainersBomAlignmentRule>()
}
