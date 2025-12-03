package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.adapter.input.rest.testing.fixtures.configuration.GraphControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.InvalidSubclassRelation
import org.orkg.graph.domain.ParentClassAlreadyExists
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [GraphControllerExceptionUnitTestConfiguration::class])
internal class ClassHierarchyExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidSubclassRelation() {
        val type = "orkg:problem:invalid_subclass_relation"
        documentedGetRequestTo(InvalidSubclassRelation(ThingId("C123"), ThingId("C456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The class "C123" cannot be a subclass of "C456".""")
            .andExpect(jsonPath("$.class_id", `is`("C123")))
            .andExpect(jsonPath("$.parent_class_id", `is`("C456")))
            .andDocument {
                responseFields<InvalidSubclassRelation>(
                    fieldWithPath("class_id").description("The id of the class.").type<ThingId>(),
                    fieldWithPath("parent_class_id").description("The id of the parent class.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun parentClassAlreadyExists() {
        val type = "orkg:problem:parent_class_already_exists"
        documentedGetRequestTo(ParentClassAlreadyExists(ThingId("C123"), ThingId("C456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The class "C123" already has a parent class (C456).""")
            .andExpect(jsonPath("$.class_id", `is`("C123")))
            .andExpect(jsonPath("$.parent_class_id", `is`("C456")))
            .andDocument {
                responseFields<ParentClassAlreadyExists>(
                    fieldWithPath("class_id").description("The id of the class.").type<ThingId>(),
                    fieldWithPath("parent_class_id").description("The id of the parent class.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
