package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.InvalidDOI
import org.orkg.contenttypes.domain.InvalidIdentifier
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class IdentifierExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidDOI() {
        val type = "orkg:problem:invalid_doi"
        documentedGetRequestTo(InvalidDOI("not a doi"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""The value passed as query parameter "doi" is not a valid DOI. The value sent was: not a doi""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/doi""")))
            .andDocumentWithValidationExceptionResponseFields(type)
    }

    @Test
    fun invalidIdentifier() {
        val type = "orkg:problem:invalid_identifier"
        documentedGetRequestTo(InvalidIdentifier("doi", IllegalArgumentException("Error")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpect(jsonPath("$.errors[0].detail", `is`("""Error""")))
            .andExpect(jsonPath("$.errors[0].pointer", `is`("""#/doi""")))
            .andDocumentWithValidationExceptionResponseFields(type)
    }
}
