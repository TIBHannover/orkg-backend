package org.orkg.common.configuration

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.orkg.common.exceptions.ErrorResponseCustomizer.Companion.errorResponseCustomizer
import org.orkg.common.exceptions.FieldError
import org.orkg.common.exceptions.createProblemURI
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.core.PropertyReferenceException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.ErrorResponse
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.URI
import java.net.URISyntaxException

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
    fun httpMessageNotReadableExceptionCustomizer(messageSource: MessageSource?) =
        errorResponseCustomizer<HttpMessageNotReadableException>(status = BAD_REQUEST) { ex, problemDetail, _ ->
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
                else -> customizeSpringException<HttpMessageNotReadableException>(
                    problemDetail = problemDetail,
                    messageSource = messageSource,
                    defaultDetail = "Failed to read request",
                )
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

    @Bean
    fun conversionNotSupportedExceptionCustomizer(messageSource: MessageSource?) =
        errorResponseCustomizer<ConversionNotSupportedException> { ex, problemDetail, _ ->
            val propertyName = ex.propertyName
            val value = ex.value
            val args = if (propertyName != null && value != null) arrayOf(propertyName, value) else null
            customizeSpringException<ConversionNotSupportedException>(
                problemDetail = problemDetail,
                messageSource = messageSource,
                defaultDetail = "Failed to convert '$propertyName' with value: '$value'",
                args = args,
            )
        }

    @Bean
    fun httpMessageNotWritableExceptionCustomizer(messageSource: MessageSource?) =
        errorResponseCustomizer<HttpMessageNotWritableException> { _, problemDetail, _ ->
            customizeSpringException<HttpMessageNotWritableException>(
                problemDetail = problemDetail,
                messageSource = messageSource,
                defaultDetail = "Failed to write request",
            )
        }

    @Bean
    fun typeMismatchExceptionCustomizer(messageSource: MessageSource?) =
        errorResponseCustomizer<TypeMismatchException>(status = BAD_REQUEST) { ex, problemDetail, _ ->
            val propertyName = ex.propertyName
            val value = ex.value
            val args = if (propertyName != null && value != null) arrayOf(propertyName, value) else null
            customizeSpringException<TypeMismatchException>(
                problemDetail = problemDetail,
                messageSource = messageSource,
                defaultDetail = "Failed to convert '$propertyName' with value: '$value'",
                args = args,
            )
        }

    @Bean
    fun illegalArgumentExceptionCustomizer() =
        errorResponseCustomizer<IllegalArgumentException> { ex, problemDetail, _ ->
            when (ex.cause) {
                is URISyntaxException -> {
                    problemDetail.type = createProblemURI("invalid_iri")
                    problemDetail.title = BAD_REQUEST.reasonPhrase
                    problemDetail.status = BAD_REQUEST.value()
                    problemDetail.detail = ex.message
                    problemDetail.setProperty("iri", (ex.cause as URISyntaxException).input)
                }
            }
        }

    @Bean
    fun uriSyntaxExceptionCustomizer() =
        errorResponseCustomizer<URISyntaxException>("invalid_iri", BAD_REQUEST) { ex, problemDetail, _ ->
            problemDetail.detail = ex.message
            problemDetail.setProperty("iri", ex.input)
        }

    private inline fun <reified T : Throwable> customizeSpringException(
        problemDetail: ProblemDetail,
        messageSource: MessageSource?,
        defaultDetail: String? = null,
        args: Array<Any>? = null,
    ) {
        if (messageSource != null) {
            val locale = LocaleContextHolder.getLocale()
            val type = messageSource.getMessage(ErrorResponse.getDefaultTypeMessageCode(T::class.java), null, null, locale)
            if (type != null) {
                problemDetail.type = URI.create(type)
            }
            val detail = messageSource.getMessage(ErrorResponse.getDefaultDetailMessageCode(T::class.java, null), args, null, locale)
            if (detail != null) {
                problemDetail.detail = detail
            }
            val title = messageSource.getMessage(ErrorResponse.getDefaultTitleMessageCode(T::class.java), null, null, locale)
            if (title != null) {
                problemDetail.title = title
            }
        }
        if (problemDetail.detail == null) {
            problemDetail.detail = defaultDetail
        }
    }
}
