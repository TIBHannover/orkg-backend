package org.orkg.common.exceptions

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.webmvc.autoconfigure.error.AbstractErrorController
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver
import org.springframework.boot.webmvc.error.ErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.WebUtils
import java.time.Clock
import java.time.OffsetDateTime

@Controller
@RequestMapping("\${server.error.path:\${error.path:/error}}")
class ErrorController(
    private val errorAttributes: ErrorAttributes,
    private val problemResponseFactory: ProblemResponseFactory,
    errorViewResolvers: List<ErrorViewResolver>,
    private val clock: Clock,
) : AbstractErrorController(errorAttributes, errorViewResolvers) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    @RequestMapping
    fun error(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val servletWebRequest = ServletWebRequest(request)
        val exception = errorAttributes.getError(servletWebRequest)!!
        val instance = servletWebRequest.getAttribute(RequestDispatcher.ERROR_REQUEST_URI, RequestAttributes.SCOPE_REQUEST) as? String
        val (httpHeaders, problemDetail) = problemResponseFactory.createProblemResponse(exception, getStatus(request), instance)

        if (exception is LoggedMessageException || problemDetail.status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            if (exception is ServiceUnavailable) {
                logger.error(exception.internalMessage)
            }
            logException(exception, request, instance ?: request.requestURI)
        }

        val status = problemDetail.status
        if (status == HttpStatus.NO_CONTENT.value()) {
            return ResponseEntity.noContent().build()
        }

        if (problemDetail.type == null) {
            problemDetail.type = ABOUT_BLANK
        }

        problemDetail.appendLegacyField("error", problemDetail.title)
        problemDetail.appendLegacyField("message", problemDetail.detail)
        problemDetail.appendLegacyField("path", problemDetail.instance)
        problemDetail.setProperty("timestamp", OffsetDateTime.now(clock))

        return ResponseEntity.status(status)
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .headers(httpHeaders)
            .body(problemDetail.toMap())
    }

    private fun ProblemDetail.appendLegacyField(legacyFieldName: String, value: Any?) {
        if (value != null && properties?.contains(legacyFieldName) != true) {
            setProperty(legacyFieldName, value)
        }
    }

    private fun logException(throwable: Throwable, request: HttpServletRequest, path: String) {
        val message = buildString {
            append(request.method)
            append(" ")
            append("Request: ")
            append(path)
            append(request.parameterMap.toParameterString())
            append(", Headers: ")
            append(request.headerMap)
            val nativeRequest = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper::class.java)
            if (nativeRequest != null) {
                append(", Payload: ")
                append(nativeRequest.contentAsString)
            }
        }
        logger.error(message, throwable)
    }

    private fun ProblemDetail.toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["type"] = type
        if (title != null) {
            map["title"] = title
        }
        map["status"] = status
        if (detail != null) {
            map["detail"] = detail
        }
        if (instance != null) {
            map["instance"] = instance
        }
        if (properties != null) {
            map += properties!!
        }
        return map
    }

    private val HttpServletRequest.headerMap: Map<String, String?> get() =
        headerNames.asSequence().associateWith { getHeader(it) }

    private fun <K, V> Map<K, Array<V>>.toParameterString() =
        when {
            entries.isNotEmpty() -> entries.joinToString(separator = "&", prefix = "?") { "${it.key}=${it.value.joinToString(separator = ",")}" }
            else -> String()
        }
}
