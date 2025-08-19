package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.domain.ListElementNotFound
import org.orkg.graph.domain.ListInUse
import org.orkg.graph.domain.ListNotFound
import org.orkg.graph.domain.ListNotModifiable
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class ListExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun listInUse() {
        documentedGetRequestTo(ListInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:list_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete list "R123" because it is used in at least one statement.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun listNotFound() {
        documentedGetRequestTo(ListNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:list_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""List "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun listNotModifiable() {
        documentedGetRequestTo(ListNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:list_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""List "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun listElementNotFound() {
        documentedGetRequestTo(ListElementNotFound())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:list_element_not_found")
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""All elements inside the list have to exist.""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("#/elements")))
            .andDocumentWithValidationExceptionResponseFields()
    }
}
