package org.orkg.common.configuration

import org.orkg.common.exceptions.ErrorController
import org.orkg.common.exceptions.ErrorResponseCustomizer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import java.time.Clock

@Configuration
class ExceptionConfiguration {
    @Bean
    fun messageSource(): MessageSource =
        ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages/exceptions")
            setDefaultEncoding(Charsets.UTF_8.name())
        }

    @Bean
    fun localValidatorFactory(messageSource: MessageSource): LocalValidatorFactoryBean =
        LocalValidatorFactoryBean().apply {
            setValidationMessageSource(messageSource)
        }

    @Bean
    fun errorController(
        serverProperties: ServerProperties,
        errorAttributes: ErrorAttributes,
        errorViewResolvers: ObjectProvider<ErrorViewResolver>,
        errorResponseCustomizers: List<ErrorResponseCustomizer<*>>,
        messageSource: MessageSource,
        clock: Clock,
    ): ErrorController =
        ErrorController(
            errorAttributes = errorAttributes,
            errorProperties = serverProperties.error,
            errorViewResolvers = errorViewResolvers.orderedStream().toList(),
            messageSource = messageSource,
            errorResponseCustomizers = errorResponseCustomizers,
            clock = clock
        )
}
