package org.orkg.testing.configuration

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.orkg.common.configuration.ErrorResponseCustomizers
import org.orkg.common.configuration.ExceptionConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * Workaround for using a custom [org.springframework.boot.web.servlet.error.ErrorController] implementation in MockMvc tests.
 * See https://github.com/spring-projects/spring-boot/issues/5574
 */
@ControllerAdvice
@Import(ErrorResponseCustomizers::class, ExceptionConfiguration::class)
class ExceptionTestConfiguration(private val errorController: BasicErrorController) {
    @ExceptionHandler(Throwable::class)
    fun defaultErrorHandler(request: HttpServletRequest): ResponseEntity<*> {
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, request.pathInfo)
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value())
        return errorController.error(request)
    }
}
