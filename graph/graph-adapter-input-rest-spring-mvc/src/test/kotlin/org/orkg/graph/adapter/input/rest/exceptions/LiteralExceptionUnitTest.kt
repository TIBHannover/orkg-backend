package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.LiteralAlreadyExists
import org.orkg.graph.domain.LiteralNotFound
import org.orkg.graph.domain.LiteralNotModifiable
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [GraphControllerExceptionUnitTestConfiguration::class])
internal class LiteralExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun literalNotModifiable() {
        val type = "orkg:problem:literal_not_modifiable"
        documentedGetRequestTo(LiteralNotModifiable(ThingId("L123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Literal "L123" is not modifiable.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDocument {
                responseFields<LiteralNotModifiable>(
                    fieldWithPath("literal_id").description("The id of the literal.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun literalNotFound() {
        val type = "orkg:problem:literal_not_found"
        documentedGetRequestTo(LiteralNotFound(ThingId("L123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Literal "L123" not found.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDocument {
                responseFields<LiteralNotFound>(
                    fieldWithPath("literal_id").description("The id of the literal.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidLiteralLabelTooLong() {
        val type = "orkg:problem:invalid_literal_label"
        documentedGetRequestTo(InvalidLiteralLabel())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A literal must be at most $MAX_LABEL_LENGTH characters long.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/label""")))
            .andDocumentWithValidationExceptionResponseFields<InvalidLiteralLabel>(type)
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
        val type = "orkg:problem:invalid_literal_datatype"
        documentedGetRequestTo(InvalidLiteralDatatype())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""A literal datatype must be a URI or a "xsd:"-prefixed type.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/datatype""")))
            .andDocumentWithValidationExceptionResponseFields<InvalidLiteralDatatype>(type)
    }

    @Test
    fun literalAlreadyExists() {
        val type = "orkg:problem:literal_already_exists"
        documentedGetRequestTo(LiteralAlreadyExists(ThingId("L123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Literal "L123" already exists.""")
            .andExpect(jsonPath("$.literal_id", `is`("L123")))
            .andDocument {
                responseFields<LiteralAlreadyExists>(
                    fieldWithPath("literal_id").description("The id of the literal.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
