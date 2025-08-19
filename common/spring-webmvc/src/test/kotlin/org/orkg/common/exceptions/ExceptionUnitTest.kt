package org.orkg.common.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

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
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingParameter_requiresAtLeastOneOf() {
        documentedGetRequestTo(MissingParameter.requiresAtLeastOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing parameter: At least one parameter out of "param1", "param2" is required.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyParameters_requiresExactlyOneOf() {
        get(TooManyParameters.requiresExactlyOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many parameters: Only exactly one out of "param1", "param2" is allowed.""")
    }

    @Test
    fun tooManyParameters_atMostOneOf() {
        get(TooManyParameters.atMostOneOf("param1", "param2"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_parameters")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many parameters: At most one out of "param1", "param2" is allowed.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unknownParameter() {
        documentedGetRequestTo(UnknownParameter("formatted-label"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown parameter "formatted-label".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unknownSortingProperty() {
        documentedGetRequestTo(UnknownSortingProperty("unknown"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown sorting property "unknown".""")
            .andDocumentWithDefaultExceptionResponseFields()
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
}
