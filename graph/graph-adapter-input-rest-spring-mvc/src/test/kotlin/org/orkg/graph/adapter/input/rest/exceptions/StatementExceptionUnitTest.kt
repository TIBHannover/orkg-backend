package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.adapter.input.rest.json.GraphJacksonModule
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
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, GraphJacksonModule::class, FixedClockConfig::class])
internal class StatementExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun statementNotModifiable() {
        val type = "orkg:problem:statement_not_modifiable"
        documentedGetRequestTo(StatementNotModifiable(StatementId("S123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Statement "S123" is not modifiable.""")
            .andExpect(jsonPath("$.statement_id", `is`("S123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("statement_id").description("The id of the statement."),
                    )
                )
            )
    }

    @Test
    fun invalidStatement_isListElementStatement() {
        val type = "orkg:problem:invalid_statement"
        documentedGetRequestTo(InvalidStatement.isListElementStatement())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A list element statement cannot be managed using the statements endpoint. Please see the documentation on how to manage lists.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
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
        val type = "orkg:problem:statement_already_exists"
        documentedGetRequestTo(StatementAlreadyExists(StatementId("S123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Statement already exists with id "S123".""")
            .andExpect(jsonPath("$.statement_id", `is`("S123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("statement_id").description("The id of the statement."),
                    )
                )
            )
    }

    @Test
    fun statementNotFound() {
        val type = "orkg:problem:statement_not_found"
        documentedGetRequestTo(StatementNotFound(StatementId("S123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Statement "S123" not found.""")
            .andExpect(jsonPath("$.statement_id", `is`("S123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("statement_id").description("The id of the statement."),
                    )
                )
            )
    }

    @Test
    fun statementSubjectNotFound() {
        val type = "orkg:problem:statement_subject_not_found"
        documentedGetRequestTo(StatementSubjectNotFound(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Subject "R123" not found.""")
            .andExpect(jsonPath("$.subject_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("subject_id").description("The id of the subject."),
                    )
                )
            )
    }

    @Test
    fun statementPredicateNotFound() {
        val type = "orkg:problem:statement_predicate_not_found"
        documentedGetRequestTo(StatementPredicateNotFound(ThingId("P123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Predicate "P123" not found.""")
            .andExpect(jsonPath("$.predicate_id", `is`("P123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("predicate_id").description("The id of the predicate."),
                    )
                )
            )
    }

    @Test
    fun statementObjectNotFound() {
        val type = "orkg:problem:statement_object_not_found"
        documentedGetRequestTo(StatementObjectNotFound(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "R123" not found.""")
            .andExpect(jsonPath("$.object_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields(type)).and(
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun statementInUse_usedInList() {
        val type = "orkg:problem:statement_in_use"
        documentedGetRequestTo(StatementInUse.usedInList())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A statement cannot be deleted when it is used in a list. Please see the documentation on how to manage lists.""")
            .andDocumentWithDefaultExceptionResponseFields(type)
    }
}
