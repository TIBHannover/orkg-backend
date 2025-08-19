package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
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
internal class ResourceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun resourceNotModifiable() {
        documentedGetRequestTo(ResourceNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:resource_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Resource "R123" is not modifiable.""")
            .andExpect(jsonPath("$.resource_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }

    @Test
    fun resourceNotFound() {
        documentedGetRequestTo(ResourceNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Resource "R123" not found.""")
            .andExpect(jsonPath("$.resource_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }

    @Test
    fun externalResourceNotFound() {
        documentedGetRequestTo(ExternalResourceNotFound("skos", "R123"))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:external_resource_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""External resource "R123" for ontology "skos" not found.""")
            .andExpect(jsonPath("$.resource_id").value("R123"))
            .andExpect(jsonPath("$.ontology_id").value("skos"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("resource_id").description("The id of the resource. (optional, either `resource_id` or `resource_uri` is present)"),
                        fieldWithPath("resource_uri").type("URI").description("The uri of the resource. (optional, either `resource_id` or `resource_uri` is present)").optional(),
                        fieldWithPath("ontology_id").description("The id of the resource ontology."),
                    )
                )
            )
    }

    @Test
    fun resourceInUse() {
        documentedGetRequestTo(ResourceInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:resource_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete resource "R123" because it is used in at least one statement.""")
            .andExpect(jsonPath("$.resource_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }

    @Test
    fun resourceAlreadyExists() {
        documentedGetRequestTo(ResourceAlreadyExists(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:resource_already_exists")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Resource "R123" already exists.""")
            .andExpect(jsonPath("$.resource_id", `is`("R123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }

    @Test
    fun invalidClassCollection() {
        documentedGetRequestTo(InvalidClassCollection(listOf(ThingId("C123"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_class_collection")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The collection of classes "[C123]" contains one or more invalid classes.""")
            .andExpect(jsonPath("$.class_ids.length()", `is`(1)))
            .andExpect(jsonPath("$.class_ids[0]", `is`("C123")))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("class_ids").description("The list of invalid classes."),
                    )
                )
            )
    }

    @Test
    fun reservedClass() {
        documentedGetRequestTo(ReservedClass(Classes.list))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:reserved_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Class "${Classes.list}" is reserved and therefor cannot be set.""")
            .andExpect(jsonPath("$.class_id", `is`(Classes.list.value)))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("class_id").description("The id of the class."),
                    )
                )
            )
    }
}
