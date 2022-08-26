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
}

@ConfigurationProperties(prefix = "orkg.features")
@ConstructorBinding
/**
 * Enable/Disable specific ORKG features.
 */
data class FeatureFlags(
    /** Use the old (legacy) model for Paper With Code data. */
    val pwcLegacyModel: Boolean = false
)

@Configuration
@EnableConfigurationProperties(FeatureFlags::class)
class FeatureFlagConfig
