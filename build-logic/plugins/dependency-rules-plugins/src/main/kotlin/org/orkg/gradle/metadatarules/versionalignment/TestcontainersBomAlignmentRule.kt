package org.orkg.gradle.metadatarules.versionalignment

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
abstract class TestcontainersBomAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group == "org.testcontainers" && id.name != "testcontainers") {
                // declare that testcontainers modules belong to the platform defined by the testcontainers BOM
                belongsTo("org.testcontainers:testcontainers-bom:${id.version}", true)
            }
        }
    }
}
