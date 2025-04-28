package org.orkg.testing.annotations

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

        private fun authentication(annotation: WithMockAuthentication): Authentication =
            JwtAuthenticationToken(
                Jwt.withTokenValue("test")
                    .header(JwtClaimNames.SUB, annotation.userId)
                    .claim("email_verified", true)
                    .claim("preferred_username", annotation.username)
                    .claim("email", annotation.email)
                    .build(),
                annotation.authorities.map(::SimpleGrantedAuthority),
                annotation.userId,
            )
    }
}
