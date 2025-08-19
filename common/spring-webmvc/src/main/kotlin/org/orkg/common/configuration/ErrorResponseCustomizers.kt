package org.orkg.common.configuration

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.orkg.common.exceptions.ErrorResponseCustomizer.Companion.errorResponseCustomizer
import org.orkg.common.exceptions.FieldError
import org.orkg.common.exceptions.createProblemURI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mapping.PropertyReferenceException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Configuration
class ErrorResponseCustomizers {
    @Bean
    fun httpMediaTypeNotAcceptableCustomizer() =
        errorResponseCustomizer<HttpMediaTypeNotAcceptableException> { ex, _, headers ->
            headers.accept = ex.supportedMediaTypes
        }

    @Bean
    fun httpMediaTypeNotSupportedExceptionCustomizer() =
        errorResponseCustomizer<HttpMediaTypeNotSupportedException> { ex, _, headers ->
            headers.accept = ex.supportedMediaTypes
        }

    @Bean
    fun httpMessageNotReadableExceptionCustomizer() =
        errorResponseCustomizer<HttpMessageNotReadableException> { ex, problemDetail, _ ->
            when (val cause = ex.cause) {
                is UnrecognizedPropertyException -> {
                    problemDetail.type = createProblemURI("unknown_json_field")
                    problemDetail.title = BAD_REQUEST.reasonPhrase
                    problemDetail.status = BAD_REQUEST.value()
                    problemDetail.detail = """Unknown field "${cause.fieldPath}"."""
                    problemDetail.setProperty("pointer", cause.jsonPointer)
                }
                is MismatchedInputException -> {
                    problemDetail.type = createProblemURI("mismatched_json_input")
                    problemDetail.title = BAD_REQUEST.reasonPhrase
                    problemDetail.status = BAD_REQUEST.value()
                    problemDetail.detail = """Field "${cause.fieldPath}" is either missing, "null", of invalid type, or contains "null" values."""
                    problemDetail.setProperty("pointer", cause.jsonPointer)
                }
                is JsonParseException -> {
                    problemDetail.type = createProblemURI("invalid_json")
                    problemDetail.title = BAD_REQUEST.reasonPhrase
                    problemDetail.status = BAD_REQUEST.value()
                    problemDetail.detail = cause.originalMessage
                }
            }
        }

    @Bean
    fun methodArgumentTypeMismatchExceptionCustomizer() =
        errorResponseCustomizer<MethodArgumentTypeMismatchException>("type_mismatch", BAD_REQUEST) { ex, problemDetail, _ ->
            problemDetail.detail = ex.rootCause?.message
        }

    @Bean
    fun accessDeniedExceptionCustomizer() =
        errorResponseCustomizer<AccessDeniedException>("access_denied", FORBIDDEN)

    @Bean
    fun propertyReferenceExceptionCustomizer() =
        errorResponseCustomizer<PropertyReferenceException>("unknown_property", BAD_REQUEST) { ex, problemDetail, _ ->
            problemDetail.detail = """Unknown property "${ex.propertyName}"."""
            problemDetail.setProperty("property", ex.propertyName)
        }

    @Bean
    fun methodArgumentNotValidExceptionCustomizer() =
        errorResponseCustomizer<MethodArgumentNotValidException>(status = BAD_REQUEST) { ex, problemDetail, _ ->
            problemDetail.setProperty("errors", ex.bindingResult.fieldErrors.map(FieldError::from))
        }
}
