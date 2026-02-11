package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.CannotDeleteIndividualRosettaStoneStatementVersion
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.MissingObjectPositionValue
import org.orkg.contenttypes.domain.MissingSubjectPositionValue
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.ObjectPositionValueDoesNotMatchPattern
import org.orkg.contenttypes.domain.ObjectPositionValueTooHigh
import org.orkg.contenttypes.domain.ObjectPositionValueTooLow
import org.orkg.contenttypes.domain.RosettaStoneStatementInUse
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.RosettaStoneStatementVersionNotFound
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.TooManyObjectPositionValues
import org.orkg.contenttypes.domain.TooManySubjectPositionValues
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class RosettaStoneStatementExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun tooManyInputPositions() {
        val type = "orkg:problem:too_many_input_positions"
        documentedGetRequestTo(TooManyInputPositions(5, 10, ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many input positions for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andExpect(jsonPath("$.expected_input_position_count").value("5"))
            .andExpect(jsonPath("$.actual_input_position_count").value("10"))
            .andDocument {
                responseFields<TooManyInputPositions>(
                    fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template.").type<ThingId>(),
                    fieldWithPath("expected_input_position_count").description("The expected number of input positions.").type<Int>(),
                    fieldWithPath("actual_input_position_count").description("The actual number of input positions.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingInputPositions() {
        val type = "orkg:problem:missing_input_positions"
        documentedGetRequestTo(MissingInputPositions(5, 2, ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andExpect(jsonPath("$.expected_input_position_count").value("5"))
            .andExpect(jsonPath("$.actual_input_position_count").value("2"))
            .andDocument {
                responseFields<MissingInputPositions>(
                    fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template.").type<ThingId>(),
                    fieldWithPath("expected_input_position_count").description("The expected number of input positions.").type<Int>(),
                    fieldWithPath("actual_input_position_count").description("The actual number of input positions.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneStatementNotModifiable() {
        val type = "orkg:problem:rosetta_stone_statement_not_modifiable"
        documentedGetRequestTo(RosettaStoneStatementNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone statement "R123" is not modifiable.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneStatementNotModifiable>(
                    fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneStatementNotFound() {
        val type = "orkg:problem:rosetta_stone_statement_not_found"
        documentedGetRequestTo(RosettaStoneStatementNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Rosetta stone statement "R123" not found.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneStatementNotFound>(
                    fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneStatementVersionNotFound() {
        val type = "orkg:problem:rosetta_stone_statement_version_not_found"
        documentedGetRequestTo(RosettaStoneStatementVersionNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Rosetta stone statement version "R123" not found.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_version_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneStatementVersionNotFound>(
                    fieldWithPath("rosetta_stone_statement_version_id").description("The id of the rosetta stone statement version.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun cannotDeleteIndividualRosettaStoneStatementVersion() {
        val type = "orkg:problem:cannot_delete_individual_rosetta_stone_statement_version"
        documentedGetRequestTo(CannotDeleteIndividualRosettaStoneStatementVersion(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Cannot delete individual versions of rosetta stone statements.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_version_id").value("R123"))
            .andDocument {
                responseFields<CannotDeleteIndividualRosettaStoneStatementVersion>(
                    fieldWithPath("rosetta_stone_statement_version_id").description("The id of the rosetta stone statement version.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneStatementInUse() {
        val type = "orkg:problem:rosetta_stone_statement_in_use"
        documentedGetRequestTo(RosettaStoneStatementInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete rosetta stone statement "R123" because it is used in at least one (rosetta stone) statement.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneStatementInUse>(
                    fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun nestedRosettaStoneStatement() {
        val type = "orkg:problem:nested_rosetta_stone_statement"
        documentedGetRequestTo(NestedRosettaStoneStatement(ThingId("R123"), 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Rosetta stone statement "R123" for input position 4 already contains a rosetta stone statement in one of its input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDocument {
                responseFields<NestedRosettaStoneStatement>(
                    fieldWithPath("rosetta_stone_statement_id").description("The id of the nested rosetta stone statement.").type<ThingId>(),
                    fieldWithPath("input_position_index").description("The index of the input position.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingSubjectPositionValue() {
        val type = "orkg:problem:missing_subject_position_value"
        documentedGetRequestTo(MissingSubjectPositionValue("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for subject position "PERSON". At least 2 input(s) are required.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("0"))
            .andExpect(jsonPath("$.min_count").value("2"))
            .andDocument {
                responseFields<MissingSubjectPositionValue>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the subject position."),
                    fieldWithPath("input_position_index").description("The index of the input position. Always `0`.").type<Int>(),
                    fieldWithPath("min_count").description("The minimum count of subject position values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingObjectPositionValue() {
        val type = "orkg:problem:missing_object_position_value"
        documentedGetRequestTo(MissingObjectPositionValue("PERSON", 3, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for object position "PERSON". At least 2 input(s) are required.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.min_count").value("2"))
            .andDocument {
                responseFields<MissingObjectPositionValue>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                    fieldWithPath("input_position_index").description("The index of the input position.").type<Int>(),
                    fieldWithPath("min_count").description("The minimum count of object position values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManySubjectPositionValue() {
        val type = "orkg:problem:too_many_subject_position_values"
        documentedGetRequestTo(TooManySubjectPositionValues("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for subject position "PERSON". Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("0"))
            .andExpect(jsonPath("$.max_count").value("2"))
            .andDocument {
                responseFields<TooManySubjectPositionValues>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the subject position."),
                    fieldWithPath("input_position_index").description("The index of the input position. Always `0`.").type<Int>(),
                    fieldWithPath("max_count").description("The maximum count of subject position values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyObjectPositionValue() {
        val type = "orkg:problem:too_many_object_position_values"
        documentedGetRequestTo(TooManyObjectPositionValues("PERSON", 3, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for object position "PERSON". Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.max_count").value("2"))
            .andDocument {
                responseFields<TooManyObjectPositionValues>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                    fieldWithPath("input_position_index").description("The index of the input position.").type<Int>(),
                    fieldWithPath("max_count").description("The maximum count of object position values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectPositionValueDoesNotMatchPattern() {
        val type = "orkg:problem:object_position_value_does_not_match_pattern"
        documentedGetRequestTo(ObjectPositionValueDoesNotMatchPattern("PERSON", 3, "2", 0, """\w+"""))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Value "2" for object position "PERSON" does not match pattern "\w+".""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("2"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.regex_pattern").value("""\w+"""))
            .andDocument {
                responseFields<ObjectPositionValueDoesNotMatchPattern>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                    fieldWithPath("input_position_index").description("The index of the object position.").type<Int>(),
                    fieldWithPath("input").description("The input provided for the object position."),
                    fieldWithPath("input_index").description("The index of the input within the input position.").type<Int>(),
                    fieldWithPath("regex_pattern").description("The pattern of the regular expression the input has to match."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectPositionValueTooLow() {
        val type = "orkg:problem:object_position_value_too_low"
        documentedGetRequestTo(ObjectPositionValueTooLow("PERSON", 3, "2", 0, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "2" for object position "PERSON" too low. Must be at least 5.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("2"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.min_inclusive").value("5"))
            .andDocument {
                responseFields<ObjectPositionValueTooLow>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                    fieldWithPath("input_position_index").description("The index of the object position.").type<Int>(),
                    fieldWithPath("input").description("The input provided for the object position."),
                    fieldWithPath("input_index").description("The index of the input within the input position.").type<Int>(),
                    fieldWithPath("min_inclusive").description("The minimum value the input can have."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectPositionValueTooHigh() {
        val type = "orkg:problem:object_position_value_too_high"
        documentedGetRequestTo(ObjectPositionValueTooHigh("PERSON", 3, "5", 0, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object position "PERSON" too high. Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("5"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.max_inclusive").value("2"))
            .andDocument {
                responseFields<ObjectPositionValueTooHigh>(
                    fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                    fieldWithPath("input_position_index").description("The index of the object position.").type<Int>(),
                    fieldWithPath("input").description("The input provided for the object position."),
                    fieldWithPath("input_index").description("The index of the input within the input position.").type<Int>(),
                    fieldWithPath("max_inclusive").description("The maximum value the input can have."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
