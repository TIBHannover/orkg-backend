package org.orkg.testing.annotations

import io.jsonwebtoken.Jwts
import org.orkg.testing.MockUserId
import org.springframework.core.annotation.AliasFor
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.lang.annotation.Inherited

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockAuthentication.Factory::class)
annotation class WithMockAuthentication(
    @get:AliasFor("value")
    val authorities: Array<String> = [],
    @get:AliasFor("authorities")
    vararg val value: String = [],
    val userId: String = MockUserId.UNKNOWN,
    val username: String = "user",
    val email: String = "user@example.org",
    @get:AliasFor(annotation = WithSecurityContext::class)
    val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD,
) {
    class Factory : WithSecurityContextFactory<WithMockAuthentication> {
        override fun createSecurityContext(annotation: WithMockAuthentication): SecurityContext =
            SecurityContextHolder.createEmptyContext().apply {
                authentication = authentication(annotation)
            }

        private fun authentication(annotation: WithMockAuthentication): Authentication {
            val subject = annotation.userId
            val claims = mapOf(
                "email_verified" to true,
                "preferred_username" to annotation.username,
                "email" to annotation.email,
                "realm_access" to mapOf(
                    "roles" to annotation.authorities.map { it.replaceFirst("ROLE_", "").lowercase() }
                )
            )
            val tokenValue = Jwts.builder()
                .subject(subject)
                .claims(claims)
                .compact()
            return JwtAuthenticationToken(
                Jwt.withTokenValue(tokenValue)
                    .header(JwtClaimNames.SUB, annotation.userId)
                    .claims { it += claims }
                    .build(),
                annotation.authorities.map(::SimpleGrantedAuthority),
                annotation.userId,
            )
        }
    }
}
