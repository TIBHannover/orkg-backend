package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.net.URISyntaxException

@ContextConfiguration(classes = [CommonSpringConfig::class, CommonDocumentationContextProvider::class])
internal class ExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun serviceUnavailable() {
        val type = "orkg:problem:service_unavailable"
        documentedGetRequestTo(ServiceUnavailable.create("TEST", 500, "irrelevant"))
            .andExpectErrorStatus(SERVICE_UNAVAILABLE)
            .andExpectType(type)
            .andExpectTitle("Service Unavailable")
            .andExpectDetail("Service unavailable.")
            .andDocumentWithDefaultExceptionResponseFields<ServiceUnavailable>(type)
    }

    @Test
    fun malformedMediaTypeCapability() {
        val type = "orkg:problem:malformed_media_type_capability"
        documentedGetRequestTo(MalformedMediaTypeCapability("formatted-label", "true"))
            .andExpectErrorStatus(HttpStatus.NOT_ACCEPTABLE)
            .andExpectType(type)
            .andExpectTitle("Not Acceptable")
            .andExpectDetail("""Malformed value "true" for media type capability "formatted-label".""")
            .andExpect(jsonPath("capability_name", `is`("formatted-label")))
            .andExpect(jsonPath("capability_value", `is`("true")))
            .andDocument {
                responseFields<MalformedMediaTypeCapability>(
                    fieldWithPath("capability_name").description("The name of the media type capability."),
                    fieldWithPath("capability_value").description("The value of the media type capability."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingParameter_requiresAtLeastOneOf() {
        val type = "orkg:problem:missing_parameter"
        documentedGetRequestTo(MissingParameter.requiresAtLeastOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing parameter: At least one parameter out of "param1", "param2" is required.""")
            .andExpect(jsonPath("parameter_names", `is`(listOf("param1", "param2"))))
            .andDocument {
                responseFields<MissingParameter>(
                    fieldWithPath("parameter_names[]").description("A list of possible parameters.").arrayItemsType("string"),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyParameters_requiresExactlyOneOf() {
        val type = "orkg:problem:too_many_parameters"
        documentedGetRequestTo(TooManyParameters.requiresExactlyOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many parameters: Only exactly one out of "param1", "param2" is allowed.""")
            .andExpect(jsonPath("parameter_names", `is`(listOf("param1", "param2"))))
            .andDocument {
                responseFields<TooManyParameters>(
                    fieldWithPath("parameter_names").description("A list of allowed parameters."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
        val type = "orkg:problem:unknown_parameter"
        documentedGetRequestTo(UnknownParameter("formatted-label"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown parameter "formatted-label".""")
            .andExpect(jsonPath("parameter_name", `is`("formatted-label")))
            .andDocument {
                responseFields<UnknownParameter>(
                    fieldWithPath("parameter_name").description("The name of the unknown parameter."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unknownSortingProperty() {
        val type = "orkg:problem:unknown_sorting_property"
        documentedGetRequestTo(UnknownSortingProperty("unknown"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown sorting property "unknown".""")
            .andExpect(jsonPath("property_name", `is`("unknown")))
            .andDocument {
                responseFields<UnknownSortingProperty>(
                    fieldWithPath("property_name").description("The name of the unknown property."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidUUID() {
        val type = "orkg:problem:invalid_uuid"
        documentedGetRequestTo(InvalidUUID("not a uuid", null))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""Value "not a uuid" is not a valid UUID.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/id""")))
            .andDocumentWithValidationExceptionResponseFields<InvalidUUID>(type)
    }

    @Test
    fun forbidden() {
        val type = "orkg:problem:forbidden"
        documentedGetRequestTo(Forbidden())
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Forbidden.""")
            .andDocumentWithDefaultExceptionResponseFields<Forbidden>(type)
    }

    @Test
    fun unauthorized() {
        val type = "orkg:problem:unauthorized"
        documentedGetRequestTo(Unauthorized())
            .andExpectErrorStatus(UNAUTHORIZED)
            .andExpectType(type)
            .andExpectTitle("Unauthorized")
            .andExpectDetail("""Unauthorized.""")
            .andDocumentWithDefaultExceptionResponseFields<Unauthorized>(type)
    }

    @Test
    fun uriSyntaxException() {
        val type = "orkg:problem:invalid_iri"
        get(URISyntaxException("http://example.org:-80", "Invalid URI"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid URI: http://example.org:-80""")
            .andExpect(jsonPath("$.iri", `is`("http://example.org:-80")))
            .andDocument {
                responseFields<URISyntaxException>(
                    fieldWithPath("iri").description("The provided iri."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
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
