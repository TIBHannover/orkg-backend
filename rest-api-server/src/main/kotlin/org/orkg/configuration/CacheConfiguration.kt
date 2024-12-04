package org.orkg.configuration

import io.github.stepio.cache.caffeine.CaffeineSpecResolver
import org.orkg.contenttypes.adapter.output.simcomp.THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE
import org.orkg.contenttypes.adapter.output.simcomp.THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE
import org.orkg.graph.adapter.output.neo4j.CLASS_ID_TO_CLASS_CACHE
import org.orkg.graph.adapter.output.neo4j.CLASS_ID_TO_CLASS_EXISTS_CACHE
import org.orkg.graph.adapter.output.neo4j.LITERAL_ID_TO_LITERAL_CACHE
import org.orkg.graph.adapter.output.neo4j.LITERAL_ID_TO_LITERAL_EXISTS_CACHE
import org.orkg.graph.adapter.output.neo4j.PREDICATE_ID_TO_PREDICATE_CACHE
import org.orkg.graph.adapter.output.neo4j.RESOURCE_ID_TO_RESOURCE_CACHE
import org.orkg.graph.adapter.output.neo4j.RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE
import org.orkg.graph.adapter.output.neo4j.THING_ID_TO_THING_CACHE
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!profileRepositories")
@Configuration
@EnableCaching
class CacheConfiguration {
    /**
     * Override default CaffeineSpecResolver bean
     */
    @Bean
    fun caffeineSpecResolver(): CaffeineSpecResolver = CustomCaffeineSpecResolver()

    @Bean
    fun caffeineCacheManagerCustomizer(): CacheManagerCustomizer<CaffeineCacheManager> =
        CacheManagerCustomizer<CaffeineCacheManager> {
            it.cacheNames = setOf(
                CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE,
                LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
                PREDICATE_ID_TO_PREDICATE_CACHE,
                RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE,
                THING_ID_TO_THING_CACHE,
                THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE,
                THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE
            )
        }
}

private const val CACHE_BASIC_SPEC = "coffee-boots.cache.basic-spec"

class CustomCaffeineSpecResolver : CaffeineSpecResolver() {
    private val logger = LoggerFactory.getLogger(this::class.java.name)
    /**
     * Creates the caffeine spec for the specified cache.
     * Uses the coffee-boots.cache.basic-spec as the default caffeine spec parameters.
     * The caffeine spec will be customized further using the coffee-boots.cache.spec.myCache parameters,
     * overriding specs defined within the basic-spec.
     */
    override fun getCaffeineSpec(name: String): String? {
        val basic = environment.getProperty(CACHE_BASIC_SPEC)
        val custom = environment.getProperty(composeKey(name))
        val spec = when {
            basic == null -> custom
            custom == null -> basic
            else -> mergeSpecs(basic, custom)
        }
        if (spec != null) {
            logger.info("""Using custom cache configuration for "$name": $spec""")
        }
        return spec
    }
}
