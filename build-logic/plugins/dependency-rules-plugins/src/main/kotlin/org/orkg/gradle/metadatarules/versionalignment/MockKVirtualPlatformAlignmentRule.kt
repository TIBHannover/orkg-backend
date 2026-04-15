package org.orkg.gradle.metadatarules.versionalignment

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

@CacheableRule
abstract class MockKVirtualPlatformAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group == "io.mockk") {
                // Declare that all MockK modules belong to a virtual platform
                belongsTo("io.mockk:mockk-virtual-platform:${id.version}", true)
            }
        }
    }
}
