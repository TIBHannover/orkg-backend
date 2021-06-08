package eu.tib.orkg.prototype.configuration

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.WebRequest

@Configuration
class ErrorAttributesConfig {
    @Bean
    fun errorAttributes(): ErrorAttributes {
        return object : DefaultErrorAttributes() {
            override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): Map<String, Any> {
                return super.getErrorAttributes(
                    webRequest,
                    options.including(
                        ErrorAttributeOptions.Include.EXCEPTION,
                        ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.BINDING_ERRORS
                    )
                )
            }
        }
    }
}
