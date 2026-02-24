package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ComparisonPathNotFound
import org.orkg.contenttypes.domain.InvalidComparisonPath
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class ComparisonTableExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidComparisonPath_statementustBeAtFirstLevel() {
        val type = "orkg:problem:invalid_comparison_path"
        documentedGetRequestTo(InvalidComparisonPath.statementMustBeAtFirstLevel(listOf(ThingId("P123"), ThingId("P456"))))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A rosetta stone statement path must be at the root.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
            .andDocument {
                responseFields<InvalidComparisonPath>(
                    fieldWithPath("comparison_path[]").description("A list of predicate ids representing the comparison path."),
                    fieldWithPath("predicate_id").type("String").description("The invalid predicate id. (optional)").optional(),
                    fieldWithPath("max_comparison_path_depth").type("Integer").description("The maximum comparison path depth. (optional)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidComparisonPath_statementValueMustBeAtSecondLevel() {
        get(InvalidComparisonPath.statementValueMustBeAtSecondLevel(listOf(ThingId("P123"), ThingId("P456"))))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_comparison_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A rosetta stone statement value path must be a children of a rosetta stone statement path.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
    }

    @Test
    fun invalidComparisonPath_statementChildMustBeStatementValue() {
        get(InvalidComparisonPath.statementChildMustBeStatementValue(listOf(ThingId("P123"), ThingId("P456"))))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_comparison_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A child path of a rosetta stone statement must be a rosetta stone statement value path.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
    }

    @Test
    fun invalidComparisonPath_statementValueCannotHaveChildren() {
        get(InvalidComparisonPath.statementValueCannotHaveChildren(listOf(ThingId("P123"), ThingId("P456"))))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_comparison_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""A rosetta stone statement value path cannot have any children.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
    }

    @Test
    fun invalidComparisonPath_invalidStatementValuePredicateId() {
        get(InvalidComparisonPath.invalidStatementValuePredicateId(listOf(ThingId("P123"), ThingId("P456")), ThingId("P456")))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_comparison_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The id "P456" is not a valid rosetta stone statement value id.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
            .andExpect(jsonPath("$.predicate_id").value("P456"))
    }

    @Test
    fun invalidComparisonPath_exceedsMaxDepth() {
        get(InvalidComparisonPath.exceedsMaxDepth(listOf(ThingId("P123"), ThingId("P456")), 10))
            .andExpectErrorStatus(HttpStatus.BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_comparison_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The comparison path exceeds the maximum depth of 10.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
            .andExpect(jsonPath("$.max_comparison_path_depth").value("10"))
    }

    @Test
    fun comparisonPathNotFound() {
        val type = "orkg:problem:comparison_path_not_found"
        documentedGetRequestTo(ComparisonPathNotFound(listOf(ThingId("P123"), ThingId("P456"))))
            .andExpectErrorStatus(HttpStatus.NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Comparison path "P123 > P456" not found.""")
            .andExpect(jsonPath("$.comparison_path.length()").value(2))
            .andExpect(jsonPath("$.comparison_path[0]").value("P123"))
            .andExpect(jsonPath("$.comparison_path[1]").value("P456"))
            .andDocument {
                responseFields<ComparisonPathNotFound>(
                    fieldWithPath("comparison_path[]").description("A list of predicate ids representing the comparison path."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
