package eu.tib.orkg.prototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@SpringBootApplication
@EnableTransactionManagement
@EnableNeo4jRepositories("eu.tib.orkg.prototype.statements.domain.model.neo4j")
@EntityScan("eu.tib.orkg.prototype.statements.domain.model.neo4j")
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)

    @Bean
    fun corsFilter(): CorsFilter {
        val config = CorsConfiguration().apply {
            allowCredentials = true
            addAllowedOrigin("http://localhost:8000")
            addAllowedHeader("*")
            addAllowedMethod("*")
        }
        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        return CorsFilter(source)
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
