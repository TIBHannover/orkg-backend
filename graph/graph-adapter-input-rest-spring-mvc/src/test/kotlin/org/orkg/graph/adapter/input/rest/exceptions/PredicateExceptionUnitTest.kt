package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExternalPredicateNotFound
import org.orkg.graph.domain.PredicateAlreadyExists
import org.orkg.graph.domain.PredicateInUse
import org.orkg.graph.domain.PredicateNotFound
import org.orkg.graph.domain.PredicateNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class PredicateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun predicateNotModifiable() {
        documentedGetRequestTo(PredicateNotModifiable(ThingId("P123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:predicate_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Predicate "P123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun predicateNotFound() {
        documentedGetRequestTo(PredicateNotFound(ThingId("P123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:predicate_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Predicate "P123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun externalPredicateNotFound() {
        documentedGetRequestTo(ExternalPredicateNotFound("skos", "P123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:external_predicate_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""External predicate "P123" for ontology "skos" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun predicateInUse() {
        documentedGetRequestTo(PredicateInUse(ThingId("P123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:predicate_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete predicate "P123" because it is used in at least one statement.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun predicateAlreadyExists() {
        documentedGetRequestTo(PredicateAlreadyExists(ThingId("P123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:predicate_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Predicate "P123" already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
