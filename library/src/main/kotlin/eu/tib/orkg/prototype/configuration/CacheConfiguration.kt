package eu.tib.orkg.prototype.configuration

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.CLASS_ID_TO_CLASS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.CLASS_ID_TO_CLASS_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.LITERAL_ID_TO_LITERAL_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.LITERAL_ID_TO_LITERAL_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.PREDICATE_ID_TO_PREDICATE_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.RESOURCE_ID_TO_RESOURCE_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.THING_ID_TO_THING_CACHE
import io.github.stepio.cache.caffeine.CaffeineSpecResolver
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
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

    @Bean
    fun caffeineCacheCustomizer(): CacheManagerCustomizer<CaffeineCacheManager> =
        CacheManagerCustomizer<CaffeineCacheManager> {
            it.setCacheNames(
                setOf(
                    CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE,
                    LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
                    PREDICATE_ID_TO_PREDICATE_CACHE,
                    RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE,
                    THING_ID_TO_THING_CACHE,
                )
            )
        }
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
