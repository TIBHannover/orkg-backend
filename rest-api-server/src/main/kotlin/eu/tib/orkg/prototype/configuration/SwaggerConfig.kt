package eu.tib.orkg.prototype.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Value("\${spring.application.name}")
    private val applicationName: String? = null
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    BEARER_KEY_SECURITY_SCHEME,
                    SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                )
            )
            .info(Info().title(applicationName))
    }

    @Bean
    fun customApi(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("api").pathsToMatch("/api/**").build()
    }

    @Bean
    fun actuatorApi(): GroupedOpenApi {
        return GroupedOpenApi.builder().group("actuator").pathsToMatch("/actuator/**").build()
    }

    companion object {
        const val BEARER_KEY_SECURITY_SCHEME = "bearer-key"
    }
}
