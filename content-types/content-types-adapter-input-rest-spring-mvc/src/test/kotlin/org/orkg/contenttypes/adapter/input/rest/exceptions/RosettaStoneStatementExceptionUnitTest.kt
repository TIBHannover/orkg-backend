package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
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
internal class RosettaStoneStatementExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun tooManyInputPositions() {
        documentedGetRequestTo(TooManyInputPositions(5, 10, ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_input_positions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many input positions for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andExpect(jsonPath("$.expected_input_position_count").value("5"))
            .andExpect(jsonPath("$.actual_input_position_count").value("10"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template."),
                        fieldWithPath("expected_input_position_count").description("The expected number of input positions."),
                        fieldWithPath("actual_input_position_count").description("The actual number of input positions."),
                    )
                )
            )
    }

    @Test
    fun missingInputPositions() {
        documentedGetRequestTo(MissingInputPositions(5, 2, ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_input_positions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andExpect(jsonPath("$.expected_input_position_count").value("5"))
            .andExpect(jsonPath("$.actual_input_position_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template."),
                        fieldWithPath("expected_input_position_count").description("The expected number of input positions."),
                        fieldWithPath("actual_input_position_count").description("The actual number of input positions."),
                    )
                )
            )
    }

    @Test
    fun rosettaStoneStatementNotModifiable() {
        documentedGetRequestTo(RosettaStoneStatementNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_statement_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone statement "R123" is not modifiable.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement."),
                    )
                )
            )
    }

    @Test
    fun rosettaStoneStatementNotFound() {
        documentedGetRequestTo(RosettaStoneStatementNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:rosetta_stone_statement_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Rosetta stone statement "R123" not found.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement."),
                    )
                )
            )
    }

    @Test
    fun rosettaStoneStatementVersionNotFound() {
        documentedGetRequestTo(RosettaStoneStatementVersionNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:rosetta_stone_statement_version_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Rosetta stone statement version "R123" not found.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_version_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_version_id").description("The id of the rosetta stone statement version."),
                    )
                )
            )
    }

    @Test
    fun cannotDeleteIndividualRosettaStoneStatementVersion() {
        documentedGetRequestTo(CannotDeleteIndividualRosettaStoneStatementVersion(ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:cannot_delete_individual_rosetta_stone_statement_version")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Cannot delete individual versions of rosetta stone statements.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_version_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_version_id").description("The id of the rosetta stone statement version."),
                    )
                )
            )
    }

    @Test
    fun rosettaStoneStatementInUse() {
        documentedGetRequestTo(RosettaStoneStatementInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_statement_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete rosetta stone statement "R123" because it is used in at least one (rosetta stone) statement.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_id").description("The id of the rosetta stone statement."),
                    )
                )
            )
    }

    @Test
    fun nestedRosettaStoneStatement() {
        documentedGetRequestTo(NestedRosettaStoneStatement(ThingId("R123"), 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:nested_rosetta_stone_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Rosetta stone statement "R123" for input position 4 already contains a rosetta stone statement in one of its input positions.""")
            .andExpect(jsonPath("$.rosetta_stone_statement_id").value("R123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("rosetta_stone_statement_id").description("The id of the nested rosetta stone statement."),
                        fieldWithPath("input_position_index").description("The index of the input position."),
                    )
                )
            )
    }

    @Test
    fun missingSubjectPositionValue() {
        documentedGetRequestTo(MissingSubjectPositionValue("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_subject_position_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for subject position "PERSON". At least 2 input(s) are required.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("0"))
            .andExpect(jsonPath("$.min_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the subject position."),
                        fieldWithPath("input_position_index").description("The index of the input position. Always `0`."),
                        fieldWithPath("min_count").description("The minimum count of subject position values."),
                    )
                )
            )
    }

    @Test
    fun missingObjectPositionValue() {
        documentedGetRequestTo(MissingObjectPositionValue("PERSON", 3, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_object_position_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for object position "PERSON". At least 2 input(s) are required.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.min_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                        fieldWithPath("input_position_index").description("The index of the input position."),
                        fieldWithPath("min_count").description("The minimum count of object position values."),
                    )
                )
            )
    }

    @Test
    fun tooManySubjectPositionValue() {
        documentedGetRequestTo(TooManySubjectPositionValues("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_subject_position_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for subject position "PERSON". Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("0"))
            .andExpect(jsonPath("$.max_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the subject position."),
                        fieldWithPath("input_position_index").description("The index of the input position. Always `0`."),
                        fieldWithPath("max_count").description("The maximum count of subject position values."),
                    )
                )
            )
    }

    @Test
    fun tooManyObjectPositionValue() {
        documentedGetRequestTo(TooManyObjectPositionValues("PERSON", 3, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_object_position_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for object position "PERSON". Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.max_count").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                        fieldWithPath("input_position_index").description("The index of the input position."),
                        fieldWithPath("max_count").description("The maximum count of object position values."),
                    )
                )
            )
    }

    @Test
    fun objectPositionValueDoesNotMatchPattern() {
        documentedGetRequestTo(ObjectPositionValueDoesNotMatchPattern("PERSON", 3, "2", 0, """\w+"""))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_does_not_match_pattern")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Value "2" for object position "PERSON" does not match pattern "\w+".""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("2"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.regex_pattern").value("""\w+"""))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                        fieldWithPath("input_position_index").description("The index of the object position."),
                        fieldWithPath("input").description("The input provided for the object position."),
                        fieldWithPath("input_index").description("The index of the input within the input position."),
                        fieldWithPath("regex_pattern").description("The pattern of the regular expression the input has to match."),
                    )
                )
            )
    }

    @Test
    fun objectPositionValueTooLow() {
        documentedGetRequestTo(ObjectPositionValueTooLow("PERSON", 3, "2", 0, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_too_low")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "2" for object position "PERSON" too low. Must be at least 5.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("2"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.min_inclusive").value("5"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                        fieldWithPath("input_position_index").description("The index of the object position."),
                        fieldWithPath("input").description("The input provided for the object position."),
                        fieldWithPath("input_index").description("The index of the input within the input position."),
                        fieldWithPath("min_inclusive").description("The minimum value the input can have."),
                    )
                )
            )
    }

    @Test
    fun objectPositionValueTooHigh() {
        documentedGetRequestTo(ObjectPositionValueTooHigh("PERSON", 3, "5", 0, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_too_high")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object position "PERSON" too high. Must be at most 2.""")
            .andExpect(jsonPath("$.input_position_placeholder").value("PERSON"))
            .andExpect(jsonPath("$.input_position_index").value("3"))
            .andExpect(jsonPath("$.input").value("5"))
            .andExpect(jsonPath("$.input_index").value("0"))
            .andExpect(jsonPath("$.max_inclusive").value("2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("input_position_placeholder").description("The placeholder of the object position."),
                        fieldWithPath("input_position_index").description("The index of the object position."),
                        fieldWithPath("input").description("The input provided for the object position."),
                        fieldWithPath("input_index").description("The index of the input within the input position."),
                        fieldWithPath("max_inclusive").description("The maximum value the input can have."),
                    )
                )
            )
    }
}
