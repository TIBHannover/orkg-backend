package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.ExternalResourceNotFound
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ResourceNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ResourceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun resourceNotModifiable() {
        documentedGetRequestTo(ResourceNotModifiable(ThingId("P123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:resource_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Resource "P123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun resourceNotFound() {
        documentedGetRequestTo(ResourceNotFound.withId(ThingId("P123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Resource "P123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun externalResourceNotFound() {
        documentedGetRequestTo(ExternalResourceNotFound("skos", "P123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:external_resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""External resource "P123" for ontology "skos" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
