package eu.tib.orkg.prototype.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.CLASS_ID_TO_CLASS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.CLASS_ID_TO_CLASS_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.LITERAL_ID_TO_LITERAL_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.LITERAL_ID_TO_LITERAL_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.PREDICATE_ID_TO_PREDICATE_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.RESOURCE_ID_TO_RESOURCE_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.RESOURCE_ID_TO_RESOURCE_EXISTS_CACHE
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.THING_ID_TO_THING_CACHE
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

typealias CaffeineBuilder = Caffeine<Any?, Any?>

@Configuration
@EnableCaching
class CacheConfiguration {
    /**
     * Cache configuration for Caffeine.
     *
     * It appears that Spring does not configure Caffeine to record statistics by default, so it is enabled
     * programmatically.
     */
    @Bean
    fun caffeineConfig(): CaffeineBuilder = Caffeine.newBuilder().recordStats()

    @Bean
    fun cacheManager(caffeine: CaffeineBuilder): CacheManager = CaffeineCacheManager().apply {
        setCaffeine(caffeine)
        setCacheNames(
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
