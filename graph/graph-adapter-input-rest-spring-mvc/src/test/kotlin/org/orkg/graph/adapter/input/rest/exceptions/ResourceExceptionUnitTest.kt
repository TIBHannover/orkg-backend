package org.orkg.graph.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExternalResourceNotFound
import org.orkg.graph.domain.InvalidClassCollection
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ResourceAlreadyExists
import org.orkg.graph.domain.ResourceInUse
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ResourceNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ResourceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun resourceNotModifiable() {
        documentedGetRequestTo(ResourceNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:resource_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Resource "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun resourceNotFound() {
        documentedGetRequestTo(ResourceNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Resource "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun externalResourceNotFound() {
        documentedGetRequestTo(ExternalResourceNotFound("skos", "R123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:external_resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""External resource "R123" for ontology "skos" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun resourceInUse() {
        documentedGetRequestTo(ResourceInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:resource_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete resource "R123" because it is used in at least one statement.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun resourceAlreadyExists() {
        documentedGetRequestTo(ResourceAlreadyExists(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:resource_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Resource "R123" already exists.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidClassCollection() {
        documentedGetRequestTo(InvalidClassCollection(listOf(ThingId("C123"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_class_collection")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The collection of classes "[C123]" contains one or more invalid classes.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun reservedClass() {
        documentedGetRequestTo(ReservedClass(Classes.list))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:reserved_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class "${Classes.list}" is reserved and therefor cannot be set.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
