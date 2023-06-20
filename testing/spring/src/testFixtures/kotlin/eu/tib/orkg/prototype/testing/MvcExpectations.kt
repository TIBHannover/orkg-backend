package eu.tib.orkg.prototype.testing

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

/**
 * Verifies that the fields of a [Page] appear in the JSON output.
 */
fun ResultActions.andExpectPage(): ResultActions = this
    .andExpect(jsonPath("$.content", `is`(notNullValue())))
    .andExpect(jsonPath("$.totalElements", `is`(notNullValue())))
    .andExpect(jsonPath("$.totalPages", `is`(notNullValue())))
    .andExpect(jsonPath("$.last", `is`(notNullValue())))
    .andExpect(jsonPath("$.first", `is`(notNullValue())))
    .andExpect(jsonPath("$.number", `is`(notNullValue())))
    .andExpect(jsonPath("$.numberOfElements", `is`(notNullValue())))
    .andExpect(jsonPath("$.size", `is`(notNullValue())))
    .andExpect(jsonPath("$.empty", `is`(notNullValue())))
    .andExpect(jsonPath("$.sort", `is`(notNullValue())))
