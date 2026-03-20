package org.orkg.common.exceptions

import jakarta.servlet.RequestDispatcher
import org.apache.catalina.connector.Request
import org.apache.catalina.connector.Response
import org.apache.catalina.valves.ErrorReportValve
import org.apache.coyote.ActionCode
import org.apache.tomcat.util.ExceptionUtils
import org.springframework.http.MediaType
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.Writer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Processes any error thrown by Tomcat and converting it into a problem detail response,
 * as described in [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html).
 * @see [org.apache.catalina.valves.JsonErrorReportValve].
 */
class ProblemDetailErrorReportValve(
    private val objectMapper: ObjectMapper,
    private val errorController: ErrorController,
) : ErrorReportValve() {
    override fun report(request: Request, response: Response, throwable: Throwable?) {
        // Do nothing on a 1xx, 2xx and 3xx status.
        // Do nothing if anything has been written already.
        // Do nothing if the response hasn't been explicitly marked as in error and that error has not been reported.
        if (response.getStatus() < 400 || response.getContentWritten() > 0 || !response.setErrorReported()) {
            return
        }

        // If an error has occurred that prevents further I/O, don't waste time
        // producing an error report that will never be read.
        val result = AtomicBoolean(false)
        response.getCoyoteResponse().action(ActionCode.IS_IO_ALLOWED, result)
        if (!result.get()) {
            return
        }

        try {
            try {
                // Set response media type and encoding.
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                response.setCharacterEncoding(Charsets.UTF_8.name())
            } catch (t: Throwable) {
                ExceptionUtils.handleThrowable(t)
                if (container.getLogger().isDebugEnabled()) {
                    container.getLogger().debug(sm.getString("errorReportValve.contentTypeFail"), t)
                }
            }
            val writer: Writer? = response.getReporter()
            if (writer != null) {
                try {
                    // Set exception attribute; Required by error controller for exception extraction.
                    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, TomcatException(throwable))
                } catch (t: Throwable) {
                    ExceptionUtils.handleThrowable(t)
                    if (container.getLogger().isDebugEnabled()) {
                        container.getLogger().debug("Failure to set the exception of request", t)
                    }
                }
                try {
                    // Write RFC 9457 conform problem detail response.
                    objectMapper.writeValue(writer, errorController.error(request).body)
                } catch (e: JacksonException) {
                    container.getLogger().error(e)
                }
                response.finishResponse()
            }
        } catch (_: IOException) {
            // ignore
        } catch (_: IllegalStateException) {
            // ignore
        }
    }
}
