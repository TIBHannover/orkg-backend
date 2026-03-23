import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition
import org.orkg.gradle.metadatarules.versionalignment.CommonMarkVirtualPlatformAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.JacksonBomAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.MockKVirtualPlatformAlignmentRule
import org.orkg.gradle.metadatarules.versionalignment.RestdocsApiSpecVirtualPlatformAlignmentRule

plugins {
    id("org.gradlex.jvm-dependency-conflict-resolution")
}

jvmDependencyConflicts {
    consistentResolution {
        platform("org.orkg:platform")
    }
    conflictResolution {
        select(CapabilityDefinition.JAKARTA_SERVLET_API, "org.apache.tomcat.embed:tomcat-embed-core")
        select(CapabilityDefinition.JAKARTA_ACTIVATION_API, "jakarta.activation:jakarta.activation-api")
        select(CapabilityDefinition.SLF4J_IMPL, "ch.qos.logback:logback-classic")
    }
    logging {
        enforceLogback()
    }
}

dependencies.components {
    all<CommonMarkVirtualPlatformAlignmentRule>()
    all<JacksonBomAlignmentRule>()
    all<MockKVirtualPlatformAlignmentRule>()
    all<RestdocsApiSpecVirtualPlatformAlignmentRule>()
//    all<TestcontainersBomAlignmentRule>()
}
