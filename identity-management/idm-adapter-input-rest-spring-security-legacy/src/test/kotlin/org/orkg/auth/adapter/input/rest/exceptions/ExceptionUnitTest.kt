package org.orkg.auth.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.auth.adapter.input.rest.LegacyAuthController
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun oAuth2Exception_invalidRequest() {
        val type = "orkg:problem:invalid_request"
        documentedGetRequestTo(LegacyAuthController.OAuth2Exception("invalid_request", "Missing grant type"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing grant type""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("error_description").description("A short, human-readable summary of the problem type.")
                    )
                )
            )
    }

    @Test
    fun oAuth2Exception_unsupportedGrantType() {
        val type = "orkg:problem:unsupported_grant_type"
        documentedGetRequestTo(LegacyAuthController.OAuth2Exception("unsupported_grant_type", "Unsupported grant type"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unsupported grant type""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("error_description").description("A short, human-readable summary of the problem type.")
                    )
                )
            )
    }
}
