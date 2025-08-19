package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.InvalidStatement
import org.orkg.graph.domain.StatementAlreadyExists
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.StatementInUse
import org.orkg.graph.domain.StatementNotFound
import org.orkg.graph.domain.StatementNotModifiable
import org.orkg.graph.domain.StatementObjectNotFound
import org.orkg.graph.domain.StatementPredicateNotFound
import org.orkg.graph.domain.StatementSubjectNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
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
    fun invalidStatement_isListElementStatement() {
        documentedGetRequestTo(InvalidStatement.isListElementStatement())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidStatement_subjectMustNotBeLiteral() {
        get(InvalidStatement.subjectMustNotBeLiteral())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Subject must not be a literal.""")
    }

    @Test
    fun invalidStatement_includesRosettaStoneStatementResource() {
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

    @Test
    fun statementNotFound() {
        documentedGetRequestTo(StatementNotFound(StatementId("S123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:statement_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Statement "S123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun statementSubjectNotFound() {
        documentedGetRequestTo(StatementSubjectNotFound(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_subject_not_found")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Subject "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun statementPredicateNotFound() {
        documentedGetRequestTo(StatementPredicateNotFound(ThingId("P123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_predicate_not_found")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Predicate "P123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun statementObjectNotFound() {
        documentedGetRequestTo(StatementObjectNotFound(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_object_not_found")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun statementInUse_usedInList() {
        documentedGetRequestTo(StatementInUse.usedInList())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:statement_in_use")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A statement cannot be deleted when it is used in a list. Please see the documentation on how to manage lists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
