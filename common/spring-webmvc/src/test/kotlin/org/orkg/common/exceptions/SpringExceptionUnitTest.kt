package org.orkg.common.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.CommonDocumentationContextProvider
import org.orkg.common.configuration.CommonSpringConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.deprecated
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.references
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.core.MethodParameter
import org.springframework.data.core.PropertyReferenceException
import org.springframework.data.core.TypeInformation
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

@ContextConfiguration(classes = [CommonSpringConfig::class, CommonDocumentationContextProvider::class])
internal class SpringExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun propertyReferenceException() {
        val type = "orkg:problem:unknown_property"
        documentedGetRequestTo(PropertyReferenceException("property", TypeInformation.OBJECT, emptyList()))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown property "property".""")
            .andDocument {
                responseFields<PropertyReferenceException>(
                    fieldWithPath("property").description("The property that failed validation."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun httpMediaTypeNotAcceptable() {
        val type = "orkg:problem:http_media_type_not_acceptable"
        documentedGetRequestTo(HttpMediaTypeNotAcceptableException(listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(NOT_ACCEPTABLE)
            .andExpectType(type)
            .andExpectTitle("Not Acceptable")
            .andExpectDetail("""Unsupported response media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
            .andDocumentWithDefaultExceptionResponseFields<HttpMediaTypeNotAcceptableException>(type)
    }

    @Test
    fun httpMediaTypeNotSupported() {
        val type = "orkg:problem:http_media_type_not_supported"
        documentedGetRequestTo(HttpMediaTypeNotSupportedException(null as? MediaType?, listOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)))
            .andExpectErrorStatus(UNSUPPORTED_MEDIA_TYPE)
            .andExpectType(type)
            .andExpectTitle("Unsupported Media Type")
            .andExpectDetail("""Unsupported request media type. Please check the 'Accept' header for a list of supported media types.""")
            .andExpect(header().string("Accept", "application/json, application/xml"))
            .andDocumentWithDefaultExceptionResponseFields<HttpMediaTypeNotSupportedException>(type)
    }

    @Test
    fun asyncRequestTimeoutException() {
        val type = "orkg:problem:async_request_timeout"
        documentedGetRequestTo(AsyncRequestTimeoutException())
            .andExpectErrorStatus(SERVICE_UNAVAILABLE)
            .andExpectType(type)
            .andExpectTitle("Service Unavailable")
            .andDocumentWithoutDetailExceptionResponseFields<AsyncRequestTimeoutException>(type)
    }

    @Test
    fun conversionNotSupportedException() {
        val type = "orkg:problem:conversion_not_supported"
        documentedGetRequestTo(ConversionNotSupportedException(null as Any?, null, null))
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType(type)
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Failed to convert 'null' with value: 'null'""")
            .andDocumentWithDefaultExceptionResponseFields<ConversionNotSupportedException>(type)
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
        val type = "orkg:problem:handler_method_validation"
        documentedGetRequestTo(handlerMethodValidationException)
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType(type)
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Validation failure""")
            .andDocumentWithDefaultExceptionResponseFields<HandlerMethodValidationException>(type)
    }

    @Test
    fun httpMessageNotReadableException() {
        val type = "orkg:problem:http_message_not_readable"
        documentedGetRequestTo(HttpMessageNotReadableException("Message not readable!", MockClientHttpResponse()))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Failed to read request""")
            .andDocumentWithDefaultExceptionResponseFields<HttpMessageNotReadableException>(type)
    }

    @Test
    fun httpMessageNotWritableException() {
        val type = "orkg:problem:http_message_not_writable"
        documentedGetRequestTo(HttpMessageNotWritableException("Not writable!"))
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType(type)
            .andExpectTitle("Internal Server Error")
            .andExpectDetail("""Failed to write request""")
            .andDocumentWithDefaultExceptionResponseFields<HttpMessageNotWritableException>(type)
    }

    @Test
    fun httpRequestMethodNotSupportedException() {
        val type = "orkg:problem:http_request_method_not_supported"
        documentedGetRequestTo(HttpRequestMethodNotSupportedException("PATCH"))
            .andExpectErrorStatus(METHOD_NOT_ALLOWED)
            .andExpectType(type)
            .andExpectTitle("Method Not Allowed")
            .andExpectDetail("""Method 'PATCH' is not supported.""")
            .andDocumentWithDefaultExceptionResponseFields<HttpRequestMethodNotSupportedException>(type)
    }

    @Test
    fun methodArgumentNotValidException() {
        val methodArgumentNotValidException = MethodArgumentNotValidException(
            MethodParameter(this::class.java.getDeclaredMethod("methodArgumentNotValidException"), -1),
            MapBindingResult(mapOf("key" to "value"), "field").apply {
                addError(org.springframework.validation.FieldError("Container", "field", "Invalid value."))
            }
        )
        val type = "orkg:problem:invalid_argument"
        documentedGetRequestTo(methodArgumentNotValidException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid request content.""")
            .andDocument {
                responseFields<MethodArgumentNotValidException>(
                    fieldWithPath("errors").description("An array that describes the details of each validation error.").arrayItemsType("object").references<FieldError>(),
                    fieldWithPath("errors[].detail").description("A description of the issue.").optional(),
                    fieldWithPath("errors[].pointer").description("A JSON Pointer that describes the location of the problem within the request's content."),
                    fieldWithPath("errors[].message").description("A description of the issue.").optional().deprecated("detail"),
                    fieldWithPath("errors[].field").description("A JSON path that describes the location of the problem within the request's content.").deprecated("pointer"),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingRequestHeaderException() {
        val missingRequestHeaderException = MissingRequestHeaderException(
            "header-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingRequestHeaderException"), -1)
        )
        val type = "orkg:problem:missing_request_header"
        documentedGetRequestTo(missingRequestHeaderException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required header 'header-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingRequestHeaderException>(type)
    }

    @Test
    fun missingServletRequestParameterException() {
        val type = "orkg:problem:missing_request_parameter"
        documentedGetRequestTo(MissingServletRequestParameterException("name", "String"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required parameter 'name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingServletRequestParameterException>(type)
    }

    @Test
    fun missingMatrixVariableException() {
        val missingMatrixVariableException = MissingMatrixVariableException(
            "variable-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingMatrixVariableException"), -1)
        )
        val type = "orkg:problem:missing_matrix_variable"
        documentedGetRequestTo(missingMatrixVariableException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required path parameter 'variable-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingMatrixVariableException>(type)
    }

    @Test
    fun missingPathVariableException() {
        val missingPathVariableException = MissingPathVariableException(
            "variable-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingPathVariableException"), -1)
        )
        val type = "orkg:problem:missing_path_variable"
        documentedGetRequestTo(missingPathVariableException)
            .andExpectErrorStatus(INTERNAL_SERVER_ERROR)
            .andExpectType(type)
            .andExpectTitle("Internal Server Error")
            .andDocumentWithDefaultExceptionResponseFields<MissingPathVariableException>(type)
    }

    @Test
    fun missingRequestCookieException() {
        val missingRequestCookieException = MissingRequestCookieException(
            "cookie-name",
            MethodParameter(this::class.java.getDeclaredMethod("missingRequestCookieException"), -1)
        )
        val type = "orkg:problem:missing_request_cookie"
        documentedGetRequestTo(missingRequestCookieException)
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required cookie 'cookie-name' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingRequestCookieException>(type)
    }

    @Test
    fun missingServletRequestPartException() {
        val type = "orkg:problem:missing_request_part"
        documentedGetRequestTo(MissingServletRequestPartException("part"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Required part 'part' is not present.""")
            .andDocumentWithDefaultExceptionResponseFields<MissingServletRequestPartException>(type)
    }

    @Test
    fun noHandlerFoundException() {
        val type = "orkg:problem:handler_not_found"
        documentedGetRequestTo(NoHandlerFoundException("GET", "localhost", HttpHeaders()))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""No endpoint GET localhost.""")
            .andDocumentWithDefaultExceptionResponseFields<NoHandlerFoundException>(type)
    }

    @Test
    fun noResourceFoundException() {
        val type = "orkg:problem:not_found"
        documentedGetRequestTo(NoResourceFoundException(HttpMethod.GET, "/resource", "/resource"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""No static resource /resource.""")
            .andDocumentWithDefaultExceptionResponseFields<NoResourceFoundException>(type)
    }

    @Test
    fun typeMismatchException() {
        val type = "orkg:problem:type_mismatch"
        documentedGetRequestTo(TypeMismatchException(null as Any?, null))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Failed to convert 'null' with value: 'null'""")
            .andDocumentWithDefaultExceptionResponseFields<TypeMismatchException>(type)
    }

    @Test
    fun accessDeniedException() {
        val type = "orkg:problem:access_denied"
        documentedGetRequestTo(AccessDeniedException("Access denied!"))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andDocumentWithoutDetailExceptionResponseFields<AccessDeniedException>(type)
    }
}
