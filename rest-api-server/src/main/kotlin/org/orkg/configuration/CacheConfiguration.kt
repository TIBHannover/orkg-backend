package org.orkg.configuration

import com.github.benmanes.caffeine.cache.Caffeine
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
import org.springframework.boot.autoconfigure.cache.CacheProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

private val cacheNames = setOf(
    CLASS_ID_TO_CLASS_CACHE, CLASS_ID_TO_CLASS_EXISTS_CACHE,
    LITERAL_ID_TO_LITERAL_CACHE, LITERAL_ID_TO_LITERAL_EXISTS_CACHE,
    PREDICATE_ID_TO_PREDICATE_CACHE,
    RESOURCE_ID_TO_RESOURCE_CACHE, RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE,
    THING_ID_TO_THING_CACHE,
    THING_ID_TO_PUBLISHED_LITERATURE_LIST_CACHE,
    THING_ID_TO_PUBLISHED_SMART_REVIEW_CACHE,
    "metrics-name-and-parameters-to-number-cache"
)

@Profile("!profileRepositories")
@Configuration
@EnableCaching
@EnableConfigurationProperties(CaffeineConfig::class)
class CacheConfiguration {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun cacheManagerCustomizer(
        cacheProperties: CacheProperties,
        caffeineConfig: CaffeineConfig
    ): CacheManagerCustomizer<CaffeineCacheManager> =
        CacheManagerCustomizer<CaffeineCacheManager> { cacheManager ->
            cacheNames.forEach { cacheName ->
                val basic = cacheProperties.caffeine.spec
                val custom = caffeineConfig.overrides?.get(cacheName)
                val spec = when {
                    basic == null -> custom
                    custom == null -> basic
                    else -> mergeSpecs(basic, custom)
                }
                logger.info("""Using cache configuration $spec for $cacheName""")
                cacheManager.registerCustomCache(cacheName, Caffeine.from(spec).build())
            }
        }

    private fun mergeSpecs(vararg specs: String): String =
        specs.map(::splitSpec)
            .fold(mutableMapOf<String, String>()) { acc, spec -> acc.apply { putAll(spec) } }.entries
            .joinToString(",") { (key, value) -> if (value.isEmpty()) { key } else "$key=$value" }

    private fun splitSpec(spec: String): Map<String, String> =
        spec.split(",")
            .map { kv -> kv.split("=") }
            .associate { kv -> specKey(kv) to specValue(kv) }

    private fun specKey(values: List<String>): String {
        if (values.isEmpty()) {
            throw IllegalArgumentException("Could not parse caffeine spec definition.")
        }
        return values.first()
    }

    private fun specValue(values: List<String>): String {
        if (values.size > 1) {
            return values[1]
        }
        return ""
    }
}

@ConfigurationProperties(prefix = "spring.cache.caffeine")
data class CaffeineConfig(
    val overrides: Map<String, String>?
)
