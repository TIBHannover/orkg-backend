package org.orkg.testing.configuration

import io.jsonwebtoken.Jwe
import io.jsonwebtoken.Jwts
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Component

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@ConditionalOnMissingBean(type = ["org.orkg.configuration.SecurityConfiguration"])
class SecurityTestConfiguration {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests { authorize(anyRequest, permitAll) }
            csrf { disable() }
        }
        return http.build()
    }
}

@Component
class UnsecureJwtDecoder : JwtDecoder {
    override fun decode(token: String): Jwt {
        val parser = Jwts.parser()
            .unsecured()
            .build()
        val jwt = parser.parse(token)
            .accept(Jwe.UNSECURED_CLAIMS)
        return Jwt.withTokenValue(token)
            .headers { it += jwt.header }
            .claims { it += jwt.payload }
            .build()
    }
}
