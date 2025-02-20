package org.orkg.testing.annotations

import org.orkg.testing.MockUserId
import org.springframework.core.annotation.AliasFor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.lang.annotation.Inherited
import java.security.Principal

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
    val name: String = MockUserId.UNKNOWN,
    @get:AliasFor(annotation = WithSecurityContext::class)
    val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD,
) {
    class Factory : WithSecurityContextFactory<WithMockAuthentication> {
        override fun createSecurityContext(annotation: WithMockAuthentication): SecurityContext =
            SecurityContextHolder.createEmptyContext().apply {
                authentication = authentication(annotation)
            }

        private fun authentication(annotation: WithMockAuthentication): Authentication = UsernamePasswordAuthenticationToken(
            Principal { annotation.name },
            "password",
            annotation.authorities.map(::SimpleGrantedAuthority)
        )
    }
}
