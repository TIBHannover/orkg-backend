package eu.tib.orkg.prototype.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var restAuthenticationEntryPoint: RestAuthenticationEntryPoint

    val customSuccessHandler = MySavedRequestAwareAuthenticationSuccessHandler()
    val customFailureHandler = SimpleUrlAuthenticationFailureHandler()

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(restAuthenticationEntryPoint)
            .and()
            .authorizeRequests()
            .antMatchers(GET, "/api/**").permitAll()
            .antMatchers("/api/**").authenticated()
            .and()
            .formLogin()
            .successHandler(customSuccessHandler)
            .failureHandler(customFailureHandler)
            .and()
            .logout()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .inMemoryAuthentication()
            .withUser("admin")
            .password(customPasswordEncoder().encode("admin")).roles("ADMIN")
            .and()
            .withUser("user")
            .password(customPasswordEncoder().encode("user")).roles("USER")
    }

    @Bean
    fun customPasswordEncoder() = BCryptPasswordEncoder()
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
 * A custom success handler that returns `200 OK` instead of `301 Moved permanently`.
 *
 * The logic is copied from its base class [SimpleUrlAuthenticationSuccessHandler] with the redirect logic removed.
 * (The class does not provide a seam to override that behavior, unfortunately.)
 */
class MySavedRequestAwareAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    private var requestCache: RequestCache = HttpSessionRequestCache()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val savedRequest = requestCache.getRequest(request, response)

        if (savedRequest == null) {
            clearAuthenticationAttributes(request)
            return
        }
        val targetUrlParam = targetUrlParameter
        if (isAlwaysUseDefaultTargetUrl || targetUrlParam != null && StringUtils.hasText(
                request.getParameter(
                    targetUrlParam
                )
            )
        ) {
            requestCache.removeRequest(request, response)
            clearAuthenticationAttributes(request)
            return
        }
        clearAuthenticationAttributes(request)
    }
}
