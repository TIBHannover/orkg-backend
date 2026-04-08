package org.orkg.testing.spring

import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcConfigurer
import org.springframework.web.context.WebApplicationContext

class MockMvcAuthenticationConfigurer : MockMvcConfigurer {
    private val mockMvcAuthenticationHeaderRequestPostProcessor = MockMvcAuthenticationHeaderRequestPostProcessor()

    override fun beforeMockMvcCreated(
        builder: ConfigurableMockMvcBuilder<*>,
        context: WebApplicationContext,
    ): RequestPostProcessor =
        mockMvcAuthenticationHeaderRequestPostProcessor
}

class MockMvcAuthenticationHeaderRequestPostProcessor : RequestPostProcessor {
    override fun postProcessRequest(request: MockHttpServletRequest): MockHttpServletRequest {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication is AbstractOAuth2TokenAuthenticationToken<*>) {
            request.addHeader(AUTHORIZATION, "Bearer ${authentication.token.tokenValue}")
        }
        return request
    }
}
