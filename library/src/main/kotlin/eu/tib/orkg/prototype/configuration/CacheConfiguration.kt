package eu.tib.orkg.prototype.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
    fun caffeineConfig() = Caffeine.newBuilder().recordStats()
}
