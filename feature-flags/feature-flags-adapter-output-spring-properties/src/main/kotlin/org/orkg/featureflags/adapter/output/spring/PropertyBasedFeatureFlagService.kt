package org.orkg.featureflags.adapter.output.spring

import org.orkg.featureflags.output.FeatureFlagService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class PropertyBasedFeatureFlagService(
    private val config: FeatureFlags
) : FeatureFlagService {
    override fun isFormattedLabelsEnabled(): Boolean = config.formattedLabels

    override fun isCacheWarmupEnabled(): Boolean = config.cacheWarmup
}

@ConfigurationProperties(prefix = "orkg.features")
/**
 * Enable/Disable specific ORKG features.
 */
data class FeatureFlags(
    /** Enable support for formatted labels. */
    val formattedLabels: Boolean = true,
    /** Use Neo4j 3.x (legacy) series. */
    val useNeo4jVersion3: Boolean = false,
    /** Enable cache warmup. */
    val cacheWarmup: Boolean = true
)

@Configuration
@EnableConfigurationProperties(FeatureFlags::class)
class FeatureFlagConfig
