package org.orkg.common.configuration

import org.apache.catalina.core.StandardHost
import org.orkg.common.exceptions.ErrorController
import org.orkg.common.exceptions.ProblemDetailErrorReportValve
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWarDeployment
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.tomcat.ConfigurableTomcatWebServerFactory
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties
import org.springframework.boot.tomcat.autoconfigure.TomcatWebServerFactoryCustomizer
import org.springframework.boot.web.server.autoconfigure.ServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import tools.jackson.databind.ObjectMapper

@Configuration
@ConditionalOnNotWarDeployment
class ErrorValveConfiguration {
    /**
     * Returns a [TomcatWebServerFactoryCustomizer] that is used to configure the Tomcat webserver pipeline.
     * Specifically, it adds a [ProblemDetailErrorReportValve], which processes any error thrown by Tomcat
     * and formats it into a problem detail response according to [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html).
     * This is necessary because by default, not every error is propagated to Spring, possibly leading to
     * error responses that are not consistent with [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html).
     * See also: [orkg-backend!1455](https://gitlab.com/TIBHannover/orkg/orkg-backend/-/merge_requests/1455).
     */
    @Bean
    fun problemDetailTomcatWebServerFactoryCustomizer(
        environment: Environment,
        serverProperties: ServerProperties,
        tomcatProperties: TomcatServerProperties,
        webProperties: WebProperties,
        tomcatWebServerFactoryCustomizer: TomcatWebServerFactoryCustomizer,
        objectMapper: ObjectMapper,
        errorController: ErrorController,
    ): TomcatWebServerFactoryCustomizer =
        object : TomcatWebServerFactoryCustomizer(environment, serverProperties, tomcatProperties, webProperties) {
            override fun getOrder(): Int = tomcatWebServerFactoryCustomizer.getOrder() + 1

            override fun customize(factory: ConfigurableTomcatWebServerFactory) {
                factory.addContextCustomizers({ context ->
                    val standardHost = context.getParent()
                    if (standardHost is StandardHost) {
                        standardHost.getPipeline().addValve(ProblemDetailErrorReportValve(objectMapper, errorController))
                        standardHost.setErrorReportValveClass(ProblemDetailErrorReportValve::class.java.getName())
                    }
                })
            }
        }
}
