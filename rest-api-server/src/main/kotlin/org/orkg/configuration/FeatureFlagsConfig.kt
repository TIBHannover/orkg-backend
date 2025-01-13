package org.orkg.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FeatureFlags::class)
class FeatureFlagConfig

/**
 * Enable/Disable specific ORKG features.
 */
@ConfigurationProperties(prefix = "orkg.features")
data class FeatureFlags(
    /** Enable cache warmup during application startup. */
    val cacheWarmup: Boolean = true
)
