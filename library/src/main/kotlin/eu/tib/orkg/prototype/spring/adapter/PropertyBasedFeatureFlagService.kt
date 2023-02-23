package eu.tib.orkg.prototype.spring.adapter

import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
class PropertyBasedFeatureFlagService(
    private val config: FeatureFlags
) : FeatureFlagService {
    override fun isPapersWithCodeLegacyModelEnabled(): Boolean = config.pwcLegacyModel

    override fun isFormattedLabelsEnabled(): Boolean = config.formattedLabels

    override fun isCacheWarmupEnabled(): Boolean = config.cacheWarmup
}

@ConfigurationProperties(prefix = "orkg.features")
@ConstructorBinding
/**
 * Enable/Disable specific ORKG features.
 */
data class FeatureFlags(
    /** Use the old (legacy) model for Paper With Code data. */
    val pwcLegacyModel: Boolean = false,
    /** Enable support for formatted labels (disabled by default - for performance reasons). */
    val formattedLabels: Boolean = false,
    /** Use Neo4j 3.x (legacy) series. */
    val useNeo4jVersion3: Boolean = false,
    /** Enable cache warmup. */
    val cacheWarmup: Boolean = true
)

@Configuration
@EnableConfigurationProperties(FeatureFlags::class)
class FeatureFlagConfig
