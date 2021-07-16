package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.configuration.InputInjection
import eu.tib.orkg.prototype.core.statements.adapters.output.ResourcePersistenceAdapter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@SpringBootApplication
@EnableConfigurationProperties(InputInjection::class)
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
class AdaptersConfiguration {
    @Bean
    fun resourceRepository() = ResourcePersistenceAdapter()
}
