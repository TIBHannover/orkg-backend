package org.orkg.graph.adapter.input.rest.exceptions

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.ExternalPredicateNotFound
import org.orkg.graph.domain.PredicateAlreadyExists
import org.orkg.graph.domain.PredicateInUse
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.PredicateNotModifiable
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
internal class PredicateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun predicateNotModifiable() {
        val type = "orkg:problem:predicate_not_modifiable"
        documentedGetRequestTo(PredicateNotModifiable(ThingId("P123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Predicate "P123" is not modifiable.""")
            .andExpect(jsonPath("$.predicate_id", `is`("P123")))
            .andDocument {
                responseFields<PredicateNotModifiable>(
                    fieldWithPath("predicate_id").description("The id of the predicate.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun predicateNotFound() {
        val type = "orkg:problem:predicate_not_found"
        documentedGetRequestTo(PredicateNotFound(ThingId("P123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Predicate "P123" not found.""")
            .andExpect(jsonPath("$.predicate_id", `is`("P123")))
            .andDocument {
                responseFields<PredicateNotFound>(
                    fieldWithPath("predicate_id").description("The id of the predicate.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun externalPredicateNotFound() {
        val type = "orkg:problem:external_predicate_not_found"
        documentedGetRequestTo(ExternalPredicateNotFound("skos", "P123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""External predicate "P123" for ontology "skos" not found.""")
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.ontology_id").value("skos"))
            .andDocument {
                responseFields<ExternalPredicateNotFound>(
                    fieldWithPath("predicate_id").description("The id of the predicate. (optional, either `predicate_id` or `predicate_uri` is present)").optional(),
                    fieldWithPath("predicate_uri").type("String").description("The uri of the predicate. (optional, either `predicate_id` or `predicate_uri` is present)").type<ParsedIRI>().optional(),
                    fieldWithPath("ontology_id").description("The id of the predicate ontology."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun predicateInUse() {
        val type = "orkg:problem:predicate_in_use"
        documentedGetRequestTo(PredicateInUse(ThingId("P123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete predicate "P123" because it is used in at least one statement.""")
            .andExpect(jsonPath("$.predicate_id", `is`("P123")))
            .andDocument {
                responseFields<PredicateInUse>(
                    fieldWithPath("predicate_id").description("The id of the predicate.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun predicateAlreadyExists() {
        val type = "orkg:problem:predicate_already_exists"
        documentedGetRequestTo(PredicateAlreadyExists(ThingId("P123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Predicate "P123" already exists.""")
            .andExpect(jsonPath("$.predicate_id", `is`("P123")))
            .andDocument {
                responseFields<PredicateAlreadyExists>(
                    fieldWithPath("predicate_id").description("The id of the predicate.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
