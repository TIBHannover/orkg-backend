package org.orkg.common.exceptions

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.ConcurrentLruCache
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.WebUtils
import java.net.URI
import java.time.Clock
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Controller
@RequestMapping("\${server.error.path:\${error.path:/error}}")
class ErrorController(
    private val errorAttributes: ErrorAttributes,
    errorViewResolvers: List<ErrorViewResolver>,
    private val messageSource: MessageSource?,
    private val errorResponseCustomizers: List<ErrorResponseCustomizer<*>>,
    private val clock: Clock,
) : AbstractErrorController(errorAttributes, errorViewResolvers) {
    private val logger = LoggerFactory.getLogger(this::class.java.name)
    private val errorResponseCustomizerCache = ConcurrentLruCache<KClass<out Throwable>, ErrorResponseCustomizer<Throwable>>(24) {
        getErrorResponseCustomizer(it) ?: ErrorResponseCustomizer.DEFAULT
    }

    @RequestMapping
    fun error(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val servletWebRequest = ServletWebRequest(request)
        val exception = errorAttributes.getError(servletWebRequest)
        val instance = servletWebRequest.getAttribute(RequestDispatcher.ERROR_REQUEST_URI, RequestAttributes.SCOPE_REQUEST) as? String
        val problemDetail = buildProblemDetail(exception, request, instance)
        val httpHeaders = HttpHeaders()
        errorResponseCustomizerCache.get(exception::class).customize(exception, problemDetail, httpHeaders)

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

    private fun buildProblemDetail(
        exception: Throwable?,
        request: HttpServletRequest,
        instance: String?,
    ): ProblemDetail {
        val problemDetail = if (exception is ErrorResponse) {
            exception.updateAndGetBody(messageSource, LocaleContextHolder.getLocale())
        } else {
            val status = getStatus(request)
            ProblemDetail.forStatus(status).apply {
                title = status.reasonPhrase
            }
        }
        if (problemDetail.title == null) {
            problemDetail.title = (HttpStatus.resolve(problemDetail.status) ?: HttpStatus.INTERNAL_SERVER_ERROR).reasonPhrase
        }
        if (problemDetail.instance == null && instance != null) {
            problemDetail.instance = URI.create(instance)
        }
        return problemDetail
    }

    @Suppress("UNCHECKED_CAST")
    private fun getErrorResponseCustomizer(ex: KClass<out Throwable>): ErrorResponseCustomizer<Throwable>? {
        val matches = errorResponseCustomizers.filterTo(mutableListOf()) { ex.isSubclassOf(it.type) }
        if (matches.size > 1) {
            val comparator = KExceptionDepthComparator(ex)
            matches.sortWith { a, b -> comparator.compare(a.type, b.type) }
        }
        return matches.firstOrNull() as? ErrorResponseCustomizer<Throwable>
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
