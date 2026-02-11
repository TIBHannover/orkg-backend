package org.orkg.gradle.metadatarules.versionalignment

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
abstract class JacksonBomAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group.startsWith("tools.jackson")) {
                // declare that Jackson modules belong to the platform defined by the Jackson BOM
                belongsTo("tools.jackson:jackson-bom:${id.version}", false)
            }
        }
    }
}
