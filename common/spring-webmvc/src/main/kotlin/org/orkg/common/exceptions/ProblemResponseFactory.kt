package org.orkg.common.exceptions

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import org.springframework.util.ConcurrentLruCache
import org.springframework.web.ErrorResponse
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Component
class ProblemResponseFactory(
    private val errorResponseCustomizers: List<ErrorResponseCustomizer<*>>,
    private val messageSource: MessageSource?,
) {
    private val errorResponseCustomizerCache =
        ConcurrentLruCache<KClass<out Throwable>, ErrorResponseCustomizer<Throwable>>(24) {
            getErrorResponseCustomizer(it) ?: ErrorResponseCustomizer.DEFAULT
        }

    fun createProblemResponse(exception: Throwable, status: HttpStatus, instance: String?): ProblemResponse {
        val problemDetail = if (exception is ErrorResponse) {
            exception.updateAndGetBody(messageSource, LocaleContextHolder.getLocale())
        } else {
            ProblemDetail.forStatus(status).apply {
                title = status.reasonPhrase
            }
        }
        if (problemDetail.type == null) {
            problemDetail.type = ABOUT_BLANK
        }
        if (problemDetail.title == null) {
            problemDetail.title = (HttpStatus.resolve(problemDetail.status) ?: HttpStatus.INTERNAL_SERVER_ERROR).reasonPhrase
        }
        if (problemDetail.instance == null && instance != null) {
            problemDetail.instance = URI.create(instance)
        }
        val httpHeaders = HttpHeaders()
        errorResponseCustomizerCache.get(exception::class).customize(exception, problemDetail, httpHeaders)
        return ProblemResponse(httpHeaders, problemDetail)
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
}
