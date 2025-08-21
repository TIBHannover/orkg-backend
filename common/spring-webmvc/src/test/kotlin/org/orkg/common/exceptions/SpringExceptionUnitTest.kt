package org.orkg.common.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.core.MethodParameter
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.data.util.TypeInformation
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.validation.MapBindingResult
import org.springframework.validation.ObjectError
import org.springframework.validation.method.MethodValidationResult
import org.springframework.validation.method.ParameterValidationResult
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingMatrixVariableException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@ContextConfiguration(classes = [CommonSpringConfig::class])
internal class SpringExceptionUnitTest : MockMvcExceptionBaseTest() {
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
    fun httpMediaTypeNotAcceptable() {
        documentedGetRequestTo(HttpMediaTypeNotAcceptableException(listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(NOT_ACCEPTABLE)
            .andExpectType("orkg:problem:http_media_type_not_acceptable")
            .andExpectTitle("Not Acceptable")
            .andExpectDetail("""Unsupported response media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun httpMediaTypeNotSupported() {
        documentedGetRequestTo(HttpMediaTypeNotSupportedException(null as? MediaType?, listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(UNSUPPORTED_MEDIA_TYPE)
            .andExpectType("orkg:problem:http_media_type_not_supported")
            .andExpectTitle("Unsupported Media Type")
            .andExpectDetail("""Unsupported request media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun asyncRequestTimeoutException() {
        documentedGetRequestTo(AsyncRequestTimeoutException())
            .andExpectErrorStatus(SERVICE_UNAVAILABLE)
            .andExpectType("orkg:problem:async_request_timeout")
            .andExpectTitle("Service Unavailable")
            .andDocumentWithoutDetailExceptionResponseFields()
    }

    @Test
    fun conversionNotSupportedException() {
        documentedGetRequestTo(ConversionNotSupportedException(null as Any?, null, null))
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType("orkg:problem:conversion_not_supported")
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Failed to convert 'null' with value: 'null'""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun handlerMethodValidationException() {
        val method = this::class.java.getDeclaredMethod("handlerMethodValidationException")
        val errors = listOf(ObjectError("this", "error message"))
        val methodParameter = MethodParameter(method, -1)
        val parameterValidationResult = ParameterValidationResult(methodParameter, null, errors, null, null, null) { _, _ -> Unit }
        val handlerMethodValidationException = HandlerMethodValidationException(
            MethodValidationResult.create(this, method, listOf(parameterValidationResult))
        )
        documentedGetRequestTo(handlerMethodValidationException)
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType("orkg:problem:handler_method_validation")
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Validation failure""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun httpMessageNotReadableException() {
        documentedGetRequestTo(HttpMessageNotReadableException("Message not readable!", MockClientHttpResponse()))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:http_message_not_readable")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Failed to read request""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun httpMessageNotWritableException() {
        documentedGetRequestTo(HttpMessageNotWritableException("Not writable!"))
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType("orkg:problem:http_message_not_writable")
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Failed to write request""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun httpRequestMethodNotSupportedException() {
        documentedGetRequestTo(HttpRequestMethodNotSupportedException("PATCH"))
            .andExpectErrorStatus(METHOD_NOT_ALLOWED)
            .andExpectType("orkg:problem:http_request_method_not_supported")
            .andExpectTitle("Method Not Allowed")
            .andExpectDetail("""Method 'PATCH' is not supported.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun methodArgumentNotValidException() {
        val methodArgumentNotValidException = MethodArgumentNotValidException(
            MethodParameter(this::class.java.getDeclaredMethod("methodArgumentNotValidException"), -1),
            MapBindingResult(mapOf("key" to "value"), "field")
        )
        documentedGetRequestTo(methodArgumentNotValidException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_argument")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid request content.""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("errors").description("An array of error descriptions.")
                    )
                )
            )
    }

    @Test
    fun missingRequestHeaderException() {
        val missingRequestHeaderException = MissingRequestHeaderException(
            "header-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingRequestHeaderException"), -1)
        )
        documentedGetRequestTo(missingRequestHeaderException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_request_header")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required header 'header-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingServletRequestParameterException() {
        documentedGetRequestTo(MissingServletRequestParameterException("name", "String"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_request_parameter")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required parameter 'name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingMatrixVariableException() {
        val missingMatrixVariableException = MissingMatrixVariableException(
            "variable-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingMatrixVariableException"), -1)
        )
        documentedGetRequestTo(missingMatrixVariableException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_matrix_variable")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required path parameter 'variable-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingPathVariableException() {
        val missingPathVariableException = MissingPathVariableException(
            "variable-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingPathVariableException"), -1)
        )
        documentedGetRequestTo(missingPathVariableException)
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType("orkg:problem:missing_path_variable")
            .andExpectTitle("Internal Server Error")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingRequestCookieException() {
        val missingRequestCookieException = MissingRequestCookieException(
            "cookie-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingRequestCookieException"), -1)
        )
        documentedGetRequestTo(missingRequestCookieException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_request_cookie")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required cookie 'cookie-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingServletRequestPartException() {
        documentedGetRequestTo(MissingServletRequestPartException("part"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_request_part")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required part 'part' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun noHandlerFoundException() {
        documentedGetRequestTo(NoHandlerFoundException("GET", "localhost", HttpHeaders()))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:handler_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""No endpoint GET localhost.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun noResourceFoundException() {
        documentedGetRequestTo(NoResourceFoundException(HttpMethod.GET, "/resource"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""No static resource /resource.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun typeMismatchException() {
        documentedGetRequestTo(TypeMismatchException(null as Any?, null))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:type_mismatch")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Failed to convert 'null' with value: 'null'""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun accessDeniedException() {
        documentedGetRequestTo(AccessDeniedException("Access denied!"))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:access_denied")
            .andExpectTitle("Forbidden")
            .andDocumentWithoutDetailExceptionResponseFields()
    }
}
