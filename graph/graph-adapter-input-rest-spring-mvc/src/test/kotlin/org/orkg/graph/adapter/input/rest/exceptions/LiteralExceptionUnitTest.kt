package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.exceptions.LiteralExceptionUnitTest.TestController
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.MAX_LABEL_LENGTH
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
internal class LiteralExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun literalNotModifiable() {
        val id = ThingId("R123")

        get("/literal-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/literal-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Literal "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literalNotFound() {
        val id = ThingId("R123")

        get("/literal-not-found")
            .param("id", id.value)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/literal-not-found"))
            .andExpect(jsonPath("$.message").value("""Literal "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteralLabelTooLong() {
        get("/invalid-literal-label-too-long")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("A literal must be at most $MAX_LABEL_LENGTH characters long."))
            .andExpect(jsonPath("$.path").value("/invalid-literal-label-too-long"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteralLabelConstraintViolation() {
        val label = "not a number"
        val datatype = "xsd:decimal"

        get("/invalid-literal-label-constraint-violation")
            .param("label", label)
            .param("datatype", datatype)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("""Literal value "$label" is not a valid "$datatype"."""))
            .andExpect(jsonPath("$.path").value("/invalid-literal-label-constraint-violation"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteralDatatype() {
        get("/invalid-literal-datatype")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("datatype"))
            .andExpect(jsonPath("$.errors[0].message").value("""A literal datatype must be a URI or a "xsd:"-prefixed type"""))
            .andExpect(jsonPath("$.path").value("/invalid-literal-datatype"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/literal-not-modifiable")
        fun literalNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw LiteralNotModifiable(id)

        @GetMapping("/literal-not-found")
        fun literalNotFound(
            @RequestParam id: ThingId,
        ): Unit = throw LiteralNotFound(id)

        @GetMapping("/invalid-literal-label-too-long")
        fun invalidLiteralLabelTooLong(): Unit = throw InvalidLiteralLabel()

        @GetMapping("/invalid-literal-label-constraint-violation")
        fun invalidLiteralLabelConstraintViolation(
            @RequestParam label: String,
            @RequestParam datatype: String,
        ): Unit = throw InvalidLiteralLabel(label, datatype)

        @GetMapping("/invalid-literal-datatype")
        fun invalidLiteralDatatype(): Unit = throw InvalidLiteralDatatype()
    }
}
