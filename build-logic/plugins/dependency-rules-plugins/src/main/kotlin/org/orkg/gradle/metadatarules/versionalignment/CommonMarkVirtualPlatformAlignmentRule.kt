package org.orkg.gradle.metadatarules.versionalignment

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
abstract class CommonMarkVirtualPlatformAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group == "org.commonmark") {
                // Declare that all CommonMark modules belong to a virtual platform
                belongsTo("org.commonmark:commonmark-virtual-platform:${id.version}", true)
            }
        }
    }
}
