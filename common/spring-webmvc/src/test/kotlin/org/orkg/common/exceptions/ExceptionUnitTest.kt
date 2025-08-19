package org.orkg.common.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.data.util.TypeInformation
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.MediaType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException

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
    fun propertyReferenceException() {
        documentedGetRequestTo(PropertyReferenceException("property", TypeInformation.OBJECT, emptyList()))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_property")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown property "property".""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("property").description("The property that failed validation."),
                    )
                )
            )
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
    fun httpMediaTypeNotAcceptable() {
        documentedGetRequestTo(HttpMediaTypeNotAcceptableException(listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(HttpStatus.NOT_ACCEPTABLE)
            .andExpectType("orkg:problem:http_media_type_not_acceptable")
            .andExpectTitle("Not Acceptable")
            .andExpectDetail("""Unsupported response media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun httpMediaTypeNotSupported() {
        documentedGetRequestTo(HttpMediaTypeNotSupportedException(null as? MediaType?, listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .andExpectType("orkg:problem:http_media_type_not_supported")
            .andExpectTitle("Unsupported Media Type")
            .andExpectDetail("""Unsupported request media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
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
}
