package org.orkg.common.configuration

import org.orkg.common.exceptions.ErrorController
import org.orkg.common.exceptions.ProblemResponseFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver
import org.springframework.boot.webmvc.error.ErrorAttributes
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
        errorAttributes: ErrorAttributes,
        errorViewResolvers: ObjectProvider<ErrorViewResolver>,
        problemResponseFactory: ProblemResponseFactory,
        clock: Clock,
    ): ErrorController =
        ErrorController(
            errorAttributes = errorAttributes,
            problemResponseFactory = problemResponseFactory,
            errorViewResolvers = errorViewResolvers.orderedStream().toList(),
            clock = clock
        )
}
