package org.orkg.common.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import kotlin.reflect.KClass

interface ErrorResponseCustomizer<T : Throwable> {
    val type: KClass<out T>

    fun customize(exception: T, problemDetail: ProblemDetail, httpHeaders: HttpHeaders)

    companion object {
        inline fun <reified T : Throwable> errorResponseCustomizer(
            type: String? = null,
            status: HttpStatus? = null,
            crossinline customizer: (exception: T, problemDetail: ProblemDetail, httpHeaders: HttpHeaders) -> Unit = { _, _, _ -> },
        ): ErrorResponseCustomizer<T> =
            object : ErrorResponseCustomizer<T> {
                override val type: KClass<out T> get() = T::class

                override fun customize(exception: T, problemDetail: ProblemDetail, httpHeaders: HttpHeaders) {
                    if (type != null) {
                        problemDetail.type = createProblemURI(type)
                    }
                    if (status != null) {
                        problemDetail.status = status.value()
                        problemDetail.title = status.reasonPhrase
                    }
                    customizer(exception, problemDetail, httpHeaders)
                }
            }

        val DEFAULT = errorResponseCustomizer<Throwable> { _, _, _ -> }
    }
}
