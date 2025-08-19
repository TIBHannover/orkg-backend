package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
import org.orkg.contenttypes.domain.RosettaStoneTemplatePropertyNotModifiable
import org.orkg.contenttypes.domain.TooManyNewRosettaStoneTemplateLabelSections
import org.orkg.graph.domain.Predicates
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class RosettaStoneTemplateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun invalidSubjectPositionPath() {
        documentedGetRequestTo(InvalidSubjectPositionPath())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subject_position_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position path. Must be "${Predicates.hasSubjectPosition}".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidObjectPositionPath() {
        documentedGetRequestTo(InvalidObjectPositionPath(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_object_position_path")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid object position path for property at index "5". Must be "${Predicates.hasObjectPosition}".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneTemplateNotModifiable() {
        documentedGetRequestTo(RosettaStoneTemplateNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_template_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone template "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneTemplatePropertyNotModifiable() {
        documentedGetRequestTo(RosettaStoneTemplatePropertyNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_template_property_not_modifiable")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone template property "R123" is not modifiable.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneTemplateInUseCantBeDeleted() {
        documentedGetRequestTo(RosettaStoneTemplateInUse.cantBeDeleted(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_template_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete rosetta stone template "R123" because it is used in at least one (rosetta stone) statement.""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("id").description("The id of the template."),
                        fieldWithPath("property").type("String").description("The property of the template. (optional)").optional(),
                    )
                )
            )
    }

    @Test
    fun rosettaStoneTemplateInUseCantUpdateProperty() {
        get(RosettaStoneTemplateInUse.cantUpdateProperty(ThingId("R123"), "abc"))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_template_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to update property "abc" of rosetta stone template "R123" because it is used in at least one rosetta stone statement.""")
    }

    @Test
    fun missingFormattedLabelPlaceholderIndex() {
        documentedGetRequestTo(MissingFormattedLabelPlaceholder(4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_formatted_label_placeholder")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing formatted label placeholder "{4}".""")
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("index").description("The index of the ropsetta stone template property. (optional, either `placeholder` or `index` is present)"),
                        fieldWithPath("placeholder").type("String").description("The placeholder of the ropsetta stone template property. (optional, either `placeholder` or `index` is present)").optional(),
                    )
                )
            )
    }

    @Test
    fun missingFormattedLabelPlaceholderPlaceholder() {
        get(MissingFormattedLabelPlaceholder("4"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_formatted_label_placeholder")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing formatted label placeholder for input position "4".""")
    }

    @Test
    fun rosettaStoneTemplateLabelMustStartWithPreviousVersion() {
        documentedGetRequestTo(RosettaStoneTemplateLabelMustStartWithPreviousVersion())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:rosetta_stone_template_label_must_start_with_previous_version")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The updated formatted label must start with the previous label.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyNewRosettaStoneTemplateLabelSections() {
        documentedGetRequestTo(TooManyNewRosettaStoneTemplateLabelSections())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_new_rosetta_stone_template_label_sections")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many new formatted label sections. Must be exactly one optional section per new template property.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties() {
        documentedGetRequestTo(RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:rosetta_stone_template_label_update_requires_new_template_properties")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The formatted label can only be updated in combination with the addition of new template properties.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun newRosettaStoneTemplateLabelSectionsMustBeOptional() {
        documentedGetRequestTo(NewRosettaStoneTemplateLabelSectionsMustBeOptional())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:new_rosetta_stone_template_label_sections_must_be_optional")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New sections of the formatted label must be optional.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun rosettaStoneTemplateLabelMustBeUpdated() {
        documentedGetRequestTo(RosettaStoneTemplateLabelMustBeUpdated())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:rosetta_stone_template_label_must_be_updated")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The formatted label must be updated when updating template properties.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun newRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage() {
        documentedGetRequestTo(NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:new_rosetta_stone_template_example_usage_must_start_with_previous_example_usage")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New example usage must start with the previous example usage.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun newRosettaStoneTemplatePropertyMustBeOptional() {
        documentedGetRequestTo(NewRosettaStoneTemplatePropertyMustBeOptional("4"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:new_rosetta_stone_template_property_must_be_optional")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New rosetta stone template property "4" must be optional.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidSubjectPositionCardinality() {
        documentedGetRequestTo(InvalidSubjectPositionCardinality())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subject_position_cardinality")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position cardinality. Minimum cardinality must be at least one.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidSubjectPositionType() {
        documentedGetRequestTo(InvalidSubjectPositionType())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subject_position_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position type. Subject position must not be a literal property.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingSubjectPosition() {
        documentedGetRequestTo(MissingSubjectPosition())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_subject_position")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing subject position. There must be at least one property with path "${Predicates.hasSubjectPosition}" that has a minimum cardinality of at least one.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingPropertyPlaceholder() {
        documentedGetRequestTo(MissingPropertyPlaceholder(4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_property_placeholder")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing placeholder for property at index "4".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
