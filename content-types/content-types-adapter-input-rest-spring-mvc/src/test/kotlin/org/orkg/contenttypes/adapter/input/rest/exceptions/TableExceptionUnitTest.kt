package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.TableExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.MissingTableHeaderValue
import org.orkg.contenttypes.domain.MissingTableRowValues
import org.orkg.contenttypes.domain.MissingTableRows
import org.orkg.contenttypes.domain.TableHeaderValueMustBeLiteral
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.domain.TableNotModifiable
import org.orkg.contenttypes.domain.TooManyTableRowValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [TestController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class TableExceptionUnitTest : MockMvcBaseTest("tables") {
    @Test
    fun tableNotFound() {
        val id = "R123"

        get("/table-not-found")
            .param("id", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/table-not-found"))
            .andExpect(jsonPath("$.message").value("""Table "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingTableRows() {
        get("/missing-table-rows")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-table-rows"))
            .andExpect(jsonPath("$.message").value("""Missing table rows. At least one rows is required."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingTableHeaderValue() {
        val index = "5"

        get("/missing-table-header-value")
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-table-header-value"))
            .andExpect(jsonPath("$.message").value("""Missing table header value at index $index."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tableHeaderValueMustBeLiteral() {
        val index = "5"

        get("/table-header-value-must-be-literal")
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/table-header-value-must-be-literal"))
            .andExpect(jsonPath("$.message").value("""Table header value at index "$index" must be a literal."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tooManyTableRowValues() {
        val index = "5"
        val expectedSize = "10"

        get("/too-many-table-row-values")
            .param("index", index)
            .param("expectedSize", expectedSize)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/too-many-table-row-values"))
            .andExpect(jsonPath("$.message").value("""Row $index has more values than the header. Expected exactly $expectedSize values based on header."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingTableRowValues() {
        val index = "5"
        val expectedSize = "10"

        get("/missing-table-row-values")
            .param("index", index)
            .param("expectedSize", expectedSize)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-table-row-values"))
            .andExpect(jsonPath("$.message").value("""Row $index has less values than the header. Expected exactly $expectedSize values based on header."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tableNotModifiable() {
        val id = "R123"

        get("/table-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/table-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Table "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/table-not-found")
        fun tableNotFound(
            @RequestParam id: ThingId,
        ): Unit = throw TableNotFound(id)

        @GetMapping("/missing-table-rows")
        fun missingTableRows(): Unit = throw MissingTableRows()

        @GetMapping("/missing-table-header-value")
        fun missingTableHeaderValue(
            @RequestParam index: Int,
        ): Unit = throw MissingTableHeaderValue(index)

        @GetMapping("/table-header-value-must-be-literal")
        fun tableHeaderValueMustBeLiteral(
            @RequestParam index: Int,
        ): Unit = throw TableHeaderValueMustBeLiteral(index)

        @GetMapping("/too-many-table-row-values")
        fun tooManyTableRowValues(
            @RequestParam index: Int,
            @RequestParam expectedSize: Int,
        ): Unit = throw TooManyTableRowValues(index, expectedSize)

        @GetMapping("/missing-table-row-values")
        fun missingTableRowValues(
            @RequestParam index: Int,
            @RequestParam expectedSize: Int,
        ): Unit = throw MissingTableRowValues(index, expectedSize)

        @GetMapping("/table-not-modifiable")
        fun tableNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw TableNotModifiable(id)
    }
}
