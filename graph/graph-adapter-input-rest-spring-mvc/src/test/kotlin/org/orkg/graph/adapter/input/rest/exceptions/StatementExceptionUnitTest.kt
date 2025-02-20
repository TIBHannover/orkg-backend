package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.exceptions.StatementExceptionUnitTest.TestController
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.StatementAlreadyExists
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotModifiable
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
internal class StatementExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun statementNotModifiable() {
        val id = StatementId("S123")

        get("/statement-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/statement-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Statement "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidStatementIsListElement() {
        get("/invalid-statement-is-list-element")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-statement-is-list-element"))
            .andExpect(jsonPath("$.message").value("A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists."))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidStatementSubjectMustNotBeLiteral() {
        get("/invalid-statement-subject-must-not-be-literal")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-statement-subject-must-not-be-literal"))
            .andExpect(jsonPath("$.message").value("Subject must not be a literal."))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidStatementRosettaStoneStatementResource() {
        get("/invalid-statement-rosetta-stone-statement-resource")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-statement-rosetta-stone-statement-resource"))
            .andExpect(jsonPath("$.message").value("A rosetta stone statement resource cannot be managed using statements endpoint. Please see the documentation on how to manage rosetta stone statements."))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun statementAlreadyExists() {
        val id = StatementId("S4565")

        get("/statement-already-exists")
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/statement-already-exists"))
            .andExpect(jsonPath("$.message").value("""Statement already exists with id "$id"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/statement-not-modifiable")
        fun statementNotModifiable(
            @RequestParam id: StatementId,
        ): Unit = throw StatementNotModifiable(id)

        @GetMapping("/invalid-statement-is-list-element")
        fun invalidStatementIsListElement(): Unit = throw InvalidStatement.isListElementStatement()

        @GetMapping("/invalid-statement-subject-must-not-be-literal")
        fun invalidStatementSubjectMustNotBeLiteral(): Unit = throw InvalidStatement.subjectMustNotBeLiteral()

        @GetMapping("/invalid-statement-rosetta-stone-statement-resource")
        fun invalidStatementRosettaStoneStatementResource(): Unit = throw InvalidStatement.includesRosettaStoneStatementResource()

        @GetMapping("/statement-already-exists")
        fun statementAlreadyExists(
            @RequestParam id: StatementId,
        ): Unit = throw StatementAlreadyExists(id)
    }
}
