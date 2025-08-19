package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.LiteralAlreadyExists
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.MAX_LABEL_LENGTH
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
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class LiteralExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun literalNotModifiable() {
        documentedGetRequestTo(LiteralNotModifiable(ThingId("L123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:literal_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literal "L123" is not modifiable.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("literal_id").description("The id of the literal."),
                    )
                )
            )
    }

    @Test
    fun literalNotFound() {
        documentedGetRequestTo(LiteralNotFound(ThingId("L123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:literal_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literal "L123" not found.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("literal_id").description("The id of the literal."),
                    )
                )
            )
    }

    @Test
    fun invalidLiteralLabelTooLong() {
        documentedGetRequestTo(InvalidLiteralLabel())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal_label")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A literal must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/label""")))
            .andDocumentWithValidationExceptionResponseFields()
    }

    @Test
    fun invalidLiteralLabelConstraintViolation() {
        // TODO: create a separate exception class?
        get(InvalidLiteralLabel("not a number", "xsd:decimal"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal_label")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""Literal value "not a number" is not a valid "xsd:decimal".""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/label""")))
    }

    @Test
    fun invalidLiteralDatatype() {
        documentedGetRequestTo(InvalidLiteralDatatype())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal_datatype")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A literal datatype must be a URI or a "xsd:"-prefixed type.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/datatype""")))
            .andDocumentWithValidationExceptionResponseFields()
    }

    @Test
    fun literalAlreadyExists() {
        documentedGetRequestTo(LiteralAlreadyExists(ThingId("L123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:literal_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Literal "L123" already exists.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("literal_id").description("The id of the literal."),
                    )
                )
            )
    }
}
