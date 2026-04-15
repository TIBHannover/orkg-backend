package org.orkg.gradle.metadatarules.versionalignment

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
abstract class RestdocsApiSpecVirtualPlatformAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group == "com.epages" && id.name.startsWith("restdocs-api-spec")) {
                // Declare that all MockK modules belong to a virtual platform
                belongsTo("com.epages:restdocs-api-spec-virtual-platform:${id.version}", true)
            }
        }
    }
}
