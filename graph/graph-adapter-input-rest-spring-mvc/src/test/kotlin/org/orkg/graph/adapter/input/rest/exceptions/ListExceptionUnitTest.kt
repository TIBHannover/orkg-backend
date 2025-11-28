package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.ListElementNotFound
import org.orkg.graph.domain.ListInUse
import org.orkg.graph.domain.ListNotFound
import org.orkg.graph.domain.ListNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class ListExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun listInUse() {
        val type = "orkg:problem:list_in_use"
        documentedGetRequestTo(ListInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete list "R123" because it is used in at least one statement.""")
            .andExpect(jsonPath("$.list_id", `is`("R123")))
            .andDocument {
                responseFields<ListInUse>(
                    fieldWithPath("list_id").description("The id of the list."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun listNotFound() {
        val type = "orkg:problem:list_not_found"
        documentedGetRequestTo(ListNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""List "R123" not found.""")
            .andExpect(jsonPath("$.list_id", `is`("R123")))
            .andDocument {
                responseFields<ListNotFound>(
                    fieldWithPath("list_id").description("The id of the list."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun listNotModifiable() {
        val type = "orkg:problem:list_not_modifiable"
        documentedGetRequestTo(ListNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""List "R123" is not modifiable.""")
            .andExpect(jsonPath("$.list_id", `is`("R123")))
            .andDocument {
                responseFields<ListNotModifiable>(
                    fieldWithPath("list_id").description("The id of the list."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun listElementNotFound() {
        val type = "orkg:problem:list_element_not_found"
        documentedGetRequestTo(ListElementNotFound())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""All elements inside the list have to exist.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("#/elements")))
            .andDocumentWithValidationExceptionResponseFields(ListElementNotFound::class, type)
    }
}
