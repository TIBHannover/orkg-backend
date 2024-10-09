package org.orkg.testing

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer

@TestConfiguration
class SecurityTestConfiguration {
    @Bean
    fun authProviderForTests(): AuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(MockUserDetailsService())
            setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder())
        }

    @Bean
    fun configureSecurityConfigurerAdapterForTests(provider: AuthenticationProvider): SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> =
        object : SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder>() {
            override fun configure(builder: AuthenticationManagerBuilder) {
                builder.authenticationProvider(provider)
            }
        }
}

@TestConfiguration
class ResourceServerTestConfiguration : ResourceServerConfigurerAdapter() {
    override fun configure(resources: ResourceServerSecurityConfigurer) {
        // set stateless flag of oauth security filter chain to false in order to disable bearer token validation for integration tests
        resources.stateless(false)
    }

    override fun configure(http: HttpSecurity) {
        // override, do nothing
    }
}
