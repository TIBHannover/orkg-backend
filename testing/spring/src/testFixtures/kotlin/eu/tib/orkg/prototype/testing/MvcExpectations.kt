package eu.tib.orkg.prototype.testing

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.springframework.data.domain.Page
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

/**
 * Verifies that the fields of a [Page] appear in the JSON output.
 */
fun ResultActions.andExpectPage(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.content", `is`(notNullValue())))
    .andExpect(jsonPath("$path.totalElements", `is`(notNullValue())))
    .andExpect(jsonPath("$path.totalPages", `is`(notNullValue())))
    .andExpect(jsonPath("$path.last", `is`(notNullValue())))
    .andExpect(jsonPath("$path.first", `is`(notNullValue())))
    .andExpect(jsonPath("$path.number", `is`(notNullValue())))
    .andExpect(jsonPath("$path.numberOfElements", `is`(notNullValue())))
    .andExpect(jsonPath("$path.size", `is`(notNullValue())))
    .andExpect(jsonPath("$path.empty", `is`(notNullValue())))
    .andExpect(jsonPath("$path.sort", `is`(notNullValue())))

fun ResultActions.andExpectStatement(path: String = "$"): ResultActions = this
    .andExpect(jsonPath("$path.id", `is`(notNullValue())))
    .andExpect(jsonPath("$path.subject", `is`(notNullValue())))
    .andExpect(jsonPath("$path.predicate", `is`(notNullValue())))
    .andExpect(jsonPath("$path.object", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_by", `is`(notNullValue())))
    .andExpect(jsonPath("$path.created_at", `is`(notNullValue())))
