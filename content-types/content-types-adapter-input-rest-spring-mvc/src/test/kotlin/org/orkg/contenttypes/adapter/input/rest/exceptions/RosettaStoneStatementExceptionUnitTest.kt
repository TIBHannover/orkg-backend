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
import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.TooManyInputPositions
import org.orkg.contenttypes.domain.TooManyObjectPositionValues
import org.orkg.contenttypes.domain.TooManySubjectPositionValues
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class RosettaStoneStatementExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun tooManyInputPositions() {
        documentedGetRequestTo(TooManyInputPositions(5, ThingId("R123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_input_positions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many input positions for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingInputPositions() {
        documentedGetRequestTo(MissingInputPositions(5, ThingId("R123"), 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_input_positions")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for 2 input positions for rosetta stone statement of template "R123". Expected exactly 5 input positions.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneStatementNotModifiable() {
        documentedGetRequestTo(RosettaStoneStatementNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_statement_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone statement "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun cannotDeleteIndividualRosettaStoneStatementVersion() {
        documentedGetRequestTo(CannotDeleteIndividualRosettaStoneStatementVersion())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:cannot_delete_individual_rosetta_stone_statement_version")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Cannot delete individual versions of rosetta stone statements.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneStatementInUse() {
        documentedGetRequestTo(RosettaStoneStatementInUse(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_statement_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete rosetta stone statement "R123" because it is used in at least one (rosetta stone) statement.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun nestedRosettaStoneStatement() {
        documentedGetRequestTo(NestedRosettaStoneStatement(ThingId("R123"), 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:nested_rosetta_stone_statement")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Rosetta stone statement "R123" for input position 4 already contains a rosetta stone statement in one of its input positions.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingSubjectPositionValue() {
        documentedGetRequestTo(MissingSubjectPositionValue("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_subject_position_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for subject position "PERSON". At least 2 input(s) are required.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingObjectPositionValue() {
        documentedGetRequestTo(MissingObjectPositionValue("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_object_position_value")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing input for object position "PERSON". At least 2 input(s) are required.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManySubjectPositionValue() {
        documentedGetRequestTo(TooManySubjectPositionValues("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_subject_position_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for subject position "PERSON". Must be at most 2.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyObjectPositionValue() {
        documentedGetRequestTo(TooManyObjectPositionValues("PERSON", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_object_position_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many inputs for object position "PERSON". Must be at most 2.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectPositionValueDoesNotMatchPattern() {
        documentedGetRequestTo(ObjectPositionValueDoesNotMatchPattern("PERSON", "2", """\w+"""))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_does_not_match_pattern")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Value "2" for object position "PERSON" does not match pattern "\w+".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectPositionValueTooLow() {
        documentedGetRequestTo(ObjectPositionValueTooLow("PERSON", "2", 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_too_low")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "2" for object position "PERSON" too low. Must be at least 5.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectPositionValueTooHigh() {
        documentedGetRequestTo(ObjectPositionValueTooHigh("PERSON", "5", 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_position_value_too_high")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object position "PERSON" too high. Must be at most 2.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
