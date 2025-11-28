package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.MissingDynamicLabelPlaceholder
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage
import org.orkg.contenttypes.domain.NewRosettaStoneTemplateLabelSectionsMustBeOptional
import org.orkg.contenttypes.domain.NewRosettaStoneTemplatePropertyMustBeOptional
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustBeUpdated
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelMustStartWithPreviousVersion
import org.orkg.contenttypes.domain.RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
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
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class RosettaStoneTemplateExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun rosettaStoneTemplateNotFound() {
        val type = "orkg:problem:rosetta_stone_template_not_found"
        documentedGetRequestTo(RosettaStoneTemplateNotFound(ThingId("R123")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Rosetta stone template "R123" not found.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneTemplateNotFound>(
                    fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidSubjectPositionPath() {
        val type = "orkg:problem:invalid_subject_position_path"
        documentedGetRequestTo(InvalidSubjectPositionPath())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position path. Must be "${Predicates.hasSubjectPosition}".""")
            .andExpect(jsonPath("$.input_position_index").value("0"))
            .andDocument {
                responseFields<InvalidSubjectPositionPath>(
                    fieldWithPath("input_position_index").description("The index of the subject position."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidObjectPositionPath() {
        val type = "orkg:problem:invalid_object_position_path"
        documentedGetRequestTo(InvalidObjectPositionPath(5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid object position path for property at index "5". Must be "${Predicates.hasObjectPosition}".""")
            .andExpect(jsonPath("$.input_position_index").value("5"))
            .andDocument {
                responseFields<InvalidObjectPositionPath>(
                    fieldWithPath("input_position_index").description("The index of the subject position."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneTemplateNotModifiable() {
        val type = "orkg:problem:rosetta_stone_template_not_modifiable"
        documentedGetRequestTo(RosettaStoneTemplateNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone template "R123" is not modifiable.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneTemplateNotModifiable>(
                    fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneTemplatePropertyNotModifiable() {
        val type = "orkg:problem:rosetta_stone_template_property_not_modifiable"
        documentedGetRequestTo(RosettaStoneTemplatePropertyNotModifiable(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Rosetta stone template property "R123" is not modifiable.""")
            .andExpect(jsonPath("$.rosetta_stone_template_property_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneTemplatePropertyNotModifiable>(
                    fieldWithPath("rosetta_stone_template_property_id").description("The id of the rosetta stone template property."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneTemplateInUse_cantBeDeleted() {
        val type = "orkg:problem:rosetta_stone_template_in_use"
        documentedGetRequestTo(RosettaStoneTemplateInUse.cantBeDeleted(ThingId("R123")))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType(type)
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to delete rosetta stone template "R123" because it is used in at least one (rosetta stone) statement.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andDocument {
                responseFields<RosettaStoneTemplateInUse>(
                    fieldWithPath("rosetta_stone_template_id").description("The id of the rosetta stone template."),
                    fieldWithPath("property").type("String").description("The property of the template. (optional)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun rosettaStoneTemplateInUse_cantUpdateProperty() {
        get(RosettaStoneTemplateInUse.cantUpdateProperty(ThingId("R123"), "abc"))
            .andExpectErrorStatus(FORBIDDEN)
            .andExpectType("orkg:problem:rosetta_stone_template_in_use")
            .andExpectTitle("Forbidden")
            .andExpectDetail("""Unable to update property "abc" of rosetta stone template "R123" because it is used in at least one rosetta stone statement.""")
            .andExpect(jsonPath("$.rosetta_stone_template_id").value("R123"))
            .andExpect(jsonPath("$.property").value("abc"))
    }

    @Test
    fun missingDynamicLabelPlaceholder_withIndex() {
        val type = "orkg:problem:missing_dynamic_label_placeholder"
        documentedGetRequestTo(MissingDynamicLabelPlaceholder(4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing dynamic label placeholder "{4}".""")
            .andExpect(jsonPath("$.input_position_index").value("4"))
            .andDocument {
                responseFields<MissingDynamicLabelPlaceholder>(
                    fieldWithPath("input_position_index").description("The index of the rosetta stone template property."),
                    fieldWithPath("input_position_placeholder").type("String").description("The placeholder of the rosetta stone template property. (optional)").optional(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingDynamicLabelPlaceholder_withPlaceholder() {
        get(MissingDynamicLabelPlaceholder(4, "4"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_dynamic_label_placeholder")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing dynamic label placeholder for input position "4".""")
            .andExpect(jsonPath("$.input_position_index").value("4"))
            .andExpect(jsonPath("$.input_position_placeholder").value("4"))
    }

    @Test
    fun rosettaStoneTemplateLabelMustStartWithPreviousVersion() {
        val type = "orkg:problem:rosetta_stone_template_label_must_start_with_previous_version"
        documentedGetRequestTo(RosettaStoneTemplateLabelMustStartWithPreviousVersion())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The updated dynamic label must start with the previous label.""")
            .andDocumentWithDefaultExceptionResponseFields(RosettaStoneTemplateLabelMustStartWithPreviousVersion::class, type)
    }

    @Test
    fun tooManyNewRosettaStoneTemplateLabelSections() {
        val type = "orkg:problem:too_many_new_rosetta_stone_template_label_sections"
        documentedGetRequestTo(TooManyNewRosettaStoneTemplateLabelSections())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many new dynamic label sections. Must be exactly one optional section per new template property.""")
            .andDocumentWithDefaultExceptionResponseFields(TooManyNewRosettaStoneTemplateLabelSections::class, type)
    }

    @Test
    fun rosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties() {
        val type = "orkg:problem:rosetta_stone_template_label_update_requires_new_template_properties"
        documentedGetRequestTo(RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The dynamic label can only be updated in combination with the addition of new template properties.""")
            .andDocumentWithDefaultExceptionResponseFields(RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties::class, type)
    }

    @Test
    fun newRosettaStoneTemplateLabelSectionsMustBeOptional() {
        val type = "orkg:problem:new_rosetta_stone_template_label_sections_must_be_optional"
        documentedGetRequestTo(NewRosettaStoneTemplateLabelSectionsMustBeOptional())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New sections of the dynamic label must be optional.""")
            .andDocumentWithDefaultExceptionResponseFields(NewRosettaStoneTemplateLabelSectionsMustBeOptional::class, type)
    }

    @Test
    fun rosettaStoneTemplateLabelMustBeUpdated() {
        val type = "orkg:problem:rosetta_stone_template_label_must_be_updated"
        documentedGetRequestTo(RosettaStoneTemplateLabelMustBeUpdated())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""The dynamic label must be updated when updating template properties.""")
            .andDocumentWithDefaultExceptionResponseFields(RosettaStoneTemplateLabelMustBeUpdated::class, type)
    }

    @Test
    fun newRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage() {
        val type = "orkg:problem:new_rosetta_stone_template_example_usage_must_start_with_previous_example_usage"
        documentedGetRequestTo(NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New example usage must start with the previous example usage.""")
            .andDocumentWithDefaultExceptionResponseFields(NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage::class, type)
    }

    @Test
    fun newRosettaStoneTemplatePropertyMustBeOptional() {
        val type = "orkg:problem:new_rosetta_stone_template_property_must_be_optional"
        documentedGetRequestTo(NewRosettaStoneTemplatePropertyMustBeOptional(4, "4"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""New rosetta stone template property "4" must be optional.""")
            .andExpect(jsonPath("$.input_position_index").value("4"))
            .andExpect(jsonPath("$.input_position_placeholder").value("4"))
            .andDocument {
                responseFields<NewRosettaStoneTemplatePropertyMustBeOptional>(
                    fieldWithPath("input_position_index").description("The index of the rosetta stone template property."),
                    fieldWithPath("input_position_placeholder").description("The placeholder of the rosetta stone template property."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidSubjectPositionCardinality() {
        val type = "orkg:problem:invalid_subject_position_cardinality"
        documentedGetRequestTo(InvalidSubjectPositionCardinality())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position cardinality. Minimum cardinality must be at least one.""")
            .andDocumentWithDefaultExceptionResponseFields(InvalidSubjectPositionCardinality::class, type)
    }

    @Test
    fun invalidSubjectPositionType() {
        val type = "orkg:problem:invalid_subject_position_type"
        documentedGetRequestTo(InvalidSubjectPositionType())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid subject position type. Subject position must not be a literal property.""")
            .andDocumentWithDefaultExceptionResponseFields(InvalidSubjectPositionType::class, type)
    }

    @Test
    fun missingSubjectPosition() {
        val type = "orkg:problem:missing_subject_position"
        documentedGetRequestTo(MissingSubjectPosition())
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing subject position. There must be at least one property with path "${Predicates.hasSubjectPosition}" that has a minimum cardinality of at least one.""")
            .andDocumentWithDefaultExceptionResponseFields(MissingSubjectPosition::class, type)
    }

    @Test
    fun missingPropertyPlaceholder() {
        val type = "orkg:problem:missing_property_placeholder"
        documentedGetRequestTo(MissingPropertyPlaceholder(4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing placeholder for property at index "4".""")
            .andExpect(jsonPath("$.input_position_index").value("4"))
            .andDocument {
                responseFields<MissingPropertyPlaceholder>(
                    fieldWithPath("input_position_index").description("The index of the rosetta stone template property."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
