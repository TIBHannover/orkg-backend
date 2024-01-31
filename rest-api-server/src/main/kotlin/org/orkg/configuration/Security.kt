package org.orkg.configuration

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.header.HeaderWriterFilter
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class AuthorizationServerConfiguration(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService
) : AuthorizationServerConfigurerAdapter() {

    private val sixteenHours = 16 * 60 * 60

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory()
            .withClient("orkg-client")
            .secret("{noop}secret")
            .authorizedGrantTypes("password")
            .scopes("read")
            .accessTokenValiditySeconds(sixteenHours)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints
            .tokenStore(tokenStore())
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
    }

    @Bean
    fun tokenStore() = InMemoryTokenStore()
}

@Configuration
class SecurityConfig {
    @Bean
    fun authProvider(userDetailsService: UserDetailsService, passwordEncoder: PasswordEncoder): AuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder)
        }

    @Bean
    fun configureGlobal(provider: AuthenticationProvider): SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder> =
        object : SecurityConfigurerAdapter<AuthenticationManager, AuthenticationManagerBuilder>() {
            override fun configure(builder: AuthenticationManagerBuilder) {
                builder.authenticationProvider(provider)
            }
        }

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
}

@Configuration
@EnableResourceServer
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class ResourceServerConfiguration : ResourceServerConfigurerAdapter() {

    // global security concerns

    // http security concerns
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/auth/register").permitAll()
            .anyRequest().permitAll() // TODO: require authentication once tested
            .and()
            .cors(withDefaults())
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
    }
}

/**
 * Class to provide an [AuthenticationManager] so OAuth2 can use the `password` grant type.
 */
@Configuration(proxyBeanMethods = false)
class AuthenticationManagerProvider : WebSecurityConfigurerAdapter() {
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    override fun configure(http: HttpSecurity) {
        http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests { it.anyRequest().permitAll() }
    }
}

/**
 * An authentication entry point that will not redirect to login, but return `401 Unauthorized` instead.
 */
@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        // TODO: Send WWW-Authenticate header? What does the standard say?
        response.sendError(SC_UNAUTHORIZED, "Unauthorized")
    }
}

/**
 * Custom CORS configuration.
 *
 * See also the documentation on CORS for [Spring MVC](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web.html#mvc-cors)
 * and [Spring Security](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#cors).
 */
@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        return UrlBasedCorsConfigurationSource().apply {
            val configuration = CorsConfiguration()
                .applyPermitDefaultValues()
                .apply {
                    allowedMethods = listOf("OPTIONS", "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE")
                    // Define safe header fields for clients. Safe-listed headers do not need to be included, see
                    // https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_response_header.
                    // (The defaults in Spring differ, see the documentation.)
                    exposedHeaders = listOf(
                        "Accept",
                        "Access-Control-Allow-Origin",
                        "Access-Control-Allow-Methods",
                        "Access-Control-Allow-Headers",
                        "Access-Control-Expose-Headers",
                        "Access-Control-Allow-Credentials",
                        "Access-Control-Max-Age",
                        "Access-Control-Request-Headers",
                        "Access-Control-Request-Method",
                        "Authorization",
                        "Location",
                        "Origin",
                        "Vary",
                    )
                }
            registerCorsConfiguration("/**", configuration)
        }
    }
}

/**
 * Work-around for the (broken?) AuthenticationServer.
 *
 * We keep the security configuration of the authorization server by configuring it via the super-class.
 * In addition we add a new [CorsFilter] after the [HeaderWriterFilter];
 * that is the same position as in other filter chains.
 * To ensure that the [CorsFilter] is configured correctly, we autowire our (custom) [CorsConfigurationSource].
 *
 * Due to autowiring magic/weirdness, the [EnableAuthorizationServer] annotation needs to be removed from the configuration.
 */
@Configuration
class AuthorizationServerWorkaround(
    private val corsConfigurationSource: CorsConfigurationSource
) : AuthorizationServerSecurityConfiguration() {

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        http.addFilterAfter(CorsFilter(corsConfigurationSource), HeaderWriterFilter::class.java)
    }
}
