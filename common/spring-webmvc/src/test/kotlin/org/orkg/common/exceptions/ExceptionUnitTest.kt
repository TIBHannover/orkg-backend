package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.net.URISyntaxException

@ContextConfiguration(classes = [CommonSpringConfig::class])
internal class ExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun serviceUnavailable() {
        documentedGetRequestTo(ServiceUnavailable.create("TEST", 500, "irrelevant"))
            .andExpectErrorStatus(SERVICE_UNAVAILABLE)
            .andExpectType("orkg:problem:service_unavailable")
            .andExpectTitle("Service Unavailable")
            .andExpectDetail("Service unavailable.")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun malformedMediaTypeCapability() {
        documentedGetRequestTo(MalformedMediaTypeCapability("formatted-label", "true"))
            .andExpectErrorStatus(HttpStatus.NOT_ACCEPTABLE)
            .andExpectType("orkg:problem:malformed_media_type_capability")
            .andExpectTitle("Not Acceptable")
            .andExpectDetail("""Malformed value "true" for media type capability "formatted-label".""")
            .andExpect(jsonPath("capability_name", `is`("formatted-label")))
            .andExpect(jsonPath("capability_value", `is`("true")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("capability_name").description("The name of the media type capability."),
                        fieldWithPath("capability_value").description("The value of the media type capability."),
                    )
                )
            )
    }

    @Test
    fun missingParameter_requiresAtLeastOneOf() {
        documentedGetRequestTo(MissingParameter.requiresAtLeastOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing parameter: At least one parameter out of "param1", "param2" is required.""")
            .andExpect(jsonPath("parameter_names", `is`(listOf("param1", "param2"))))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("parameter_names").description("A list of possible parameters.")
                    )
                )
            )
    }

    @Test
    fun tooManyParameters_requiresExactlyOneOf() {
        documentedGetRequestTo(TooManyParameters.requiresExactlyOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many parameters: Only exactly one out of "param1", "param2" is allowed.""")
            .andExpect(jsonPath("parameter_names", `is`(listOf("param1", "param2"))))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("parameter_names").description("A list of allowed parameters.")
                    )
                )
            )
    }

    @Test
    fun tooManyParameters_atMostOneOf() {
        get(TooManyParameters.atMostOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many parameters: At most one out of "param1", "param2" is allowed.""")
            .andExpect(jsonPath("parameter_names", `is`(listOf("param1", "param2"))))
    }

    @Test
    fun unknownParameter() {
        documentedGetRequestTo(UnknownParameter("formatted-label"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown parameter "formatted-label".""")
            .andExpect(jsonPath("parameter_name", `is`("formatted-label")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("parameter_name").description("The name of the unknown parameter.")
                    )
                )
            )
    }

    @Test
    fun unknownSortingProperty() {
        documentedGetRequestTo(UnknownSortingProperty("unknown"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown sorting property "unknown".""")
            .andExpect(jsonPath("property_name", `is`("unknown")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("property_name").description("The name of the unknown property.")
                    )
                )
            )
    }

    @Test
    fun invalidUUID() {
        documentedGetRequestTo(InvalidUUID("not a uuid", null))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_uuid")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""Value "not a uuid" is not a valid UUID.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/id""")))
            .andDocumentWithValidationExceptionResponseFields()
    }

    @Test
    fun forbidden() {
        documentedGetRequestTo(Forbidden())
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:forbidden")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Forbidden.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unauthorized() {
        documentedGetRequestTo(Unauthorized())
            .andExpectErrorStatus(UNAUTHORIZED)
            .andExpectType("orkg:problem:unauthorized")
            .andExpectTitle("Unauthorized")
            .andExpectDetail("""Unauthorized.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun uriSyntaxException() {
        get(URISyntaxException("http://example.org:-80", "Invalid URI"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_iri")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid URI: http://example.org:-80""")
            .andExpect(jsonPath("$.iri", `is`("http://example.org:-80")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("iri").description("The provided iri.")
                    )
                )
            )
    }

    @Test
    fun illegalArgumentException_causedByURISyntaxException() {
        get(IllegalArgumentException("Invalid URI: http://example.org:-80", URISyntaxException("http://example.org:-80", "Invalid URI")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_iri")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid URI: http://example.org:-80""")
            .andExpect(jsonPath("$.iri", `is`("http://example.org:-80")))
    }
}
