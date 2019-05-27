package eu.tib.orkg.prototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.neo4j.annotation.EnableNeo4jAuditing
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.Optional

@SpringBootApplication
@EnableTransactionManagement
@EnableNeo4jRepositories("eu.tib.orkg.prototype.statements.domain.model.neo4j")
@EntityScan("eu.tib.orkg.prototype.statements.domain.model.neo4j")
@EnableNeo4jAuditing(dateTimeProviderRef = "zonedDateTimeProvider")
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)

    @Bean
    fun corsFilter(): FilterRegistrationBean<CorsFilter> {
        val config = CorsConfiguration().apply {
            allowCredentials = true
            addAllowedOrigin("http://localhost:3000")
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        val bean = FilterRegistrationBean(CorsFilter(source))
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE)
        return bean
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Component("zonedDateTimeProvider")
class ZonedDateTimeProvider : DateTimeProvider {
    override fun getNow(): Optional<TemporalAccessor> {
        // For future updates, any change to the date format can be done here
        // and reflected on the type of the Date object in the separate classes
        return Optional.of(OffsetDateTime.now())
    }
}
