package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.StatementAlreadyExists
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class StatementExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun statementNotModifiable() {
        documentedGetRequestTo(StatementNotModifiable(StatementId("S123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:statement_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Statement "S123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidStatementIsListElement() {
        documentedGetRequestTo(InvalidStatement.isListElementStatement())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidStatementSubjectMustNotBeLiteral() {
        get(InvalidStatement.subjectMustNotBeLiteral())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Subject must not be a literal.""")
    }

    @Test
    fun invalidStatementRosettaStoneStatementResource() {
        get(InvalidStatement.includesRosettaStoneStatementResource())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A rosetta stone statement resource cannot be managed using statements endpoint. Please see the documentation on how to manage rosetta stone statements.""")
    }

    @Test
    fun statementAlreadyExists() {
        documentedGetRequestTo(StatementAlreadyExists(StatementId("S123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Statement already exists with id "S123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
