package eu.tib.orkg.prototype.configuration

import io.github.stepio.cache.caffeine.CaffeineSpecResolver
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfiguration {
    /**
     * Override default CaffeineSpecResolver bean
     */
    @Bean
    fun caffeineSpecResolver(): CaffeineSpecResolver = CustomCaffeineSpecResolver()
}

private const val CACHE_BASIC_SPEC = "coffee-boots.cache.basic-spec"

class CustomCaffeineSpecResolver : CaffeineSpecResolver() {
    /**
     * Creates the caffeine spec for the specified cache.
     * Uses the coffee-boots.cache.basic-spec as the default caffeine spec parameters.
     * The caffeine spec will be customized further using the coffee-boots.cache.spec.myCache parameters,
     * overriding specs defined within the basic-spec.
     */
    override fun getCaffeineSpec(name: String): String? {
        val basic = environment.getProperty(CACHE_BASIC_SPEC)
        val custom = environment.getProperty(composeKey(name))
        return when {
            basic == null -> custom
            custom == null -> basic
            else -> mergeSpecs(basic, custom)
        }
    }
}
