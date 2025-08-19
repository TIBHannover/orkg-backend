package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.MismatchedDataType
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIsNotAClass
import org.orkg.contenttypes.domain.ObjectIsNotAList
import org.orkg.contenttypes.domain.ObjectIsNotALiteral
import org.orkg.contenttypes.domain.ObjectIsNotAPredicate
import org.orkg.contenttypes.domain.ObjectMustNotBeALiteral
import org.orkg.contenttypes.domain.ResourceIsNotAnInstanceOfTargetClass
import org.orkg.contenttypes.domain.TemplateInstanceNotFound
import org.orkg.contenttypes.domain.TemplateNotApplicable
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.UnrelatedTemplateProperty
import org.orkg.graph.domain.Classes
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [CommonJacksonModule::class, FixedClockConfig::class])
internal class TemplateInstanceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateNotApplicable() {
        documentedGetRequestTo(TemplateNotApplicable(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_not_applicable")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template "R123" cannot be applied to resource "R456" because the target resource is not an instance of the template target class.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.resource_id").value("R456"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_id").description("The id of the template."),
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }

    @Test
    fun objectIsNotAClass() {
        documentedGetRequestTo(ObjectIsNotAClass(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a class.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun objectIsNotAPredicate() {
        documentedGetRequestTo(ObjectIsNotAPredicate(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_predicate")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a predicate.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun objectIsNotAList() {
        documentedGetRequestTo(ObjectIsNotAList(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_list")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a list.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun objectIsNotALiteral() {
        documentedGetRequestTo(ObjectIsNotALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a literal.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun objectMustNotBeALiteral() {
        documentedGetRequestTo(ObjectMustNotBeALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_must_not_be_a_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" must not be a literal.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                    )
                )
            )
    }

    @Test
    fun resourceIsNotAnInstanceOfTargetClass() {
        documentedGetRequestTo(ResourceIsNotAnInstanceOfTargetClass(ThingId("R123"), ThingId("P123"), "#temp1", ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:resource_is_not_an_instance_of_target_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not an instance of target class "C123".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.template_property_target_class").value("C123"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("template_property_target_class").description("The target class of the template property."),
                    )
                )
            )
    }

    @Test
    fun labelDoesNotMatchPattern() {
        documentedGetRequestTo(LabelDoesNotMatchPattern(ThingId("R123"), "#temp1", ThingId("P123"), "label", "\\d+"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:label_does_not_match_pattern")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Label "label" for object "#temp1" for property "R123" with predicate "P123" does not match pattern "\d+".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("label"))
            .andExpect(jsonPath("$.regex_pattern").value("\\d+"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("object_label").description("The provided label of the object."),
                        fieldWithPath("regex_pattern").description("The regex pattern the object label has to match."),
                    )
                )
            )
    }

    @Test
    fun unknownTemplateProperties() {
        documentedGetRequestTo(UnknownTemplateProperties(ThingId("R123"), setOf(ThingId("R1"), ThingId("R2"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_template_properties")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown properties for template "R123": "R1", "R2".""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.template_property_ids.length()").value("2"))
            .andExpect(jsonPath("$.template_property_ids[0]").value("R1"))
            .andExpect(jsonPath("$.template_property_ids[1]").value("R2"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_id").description("The id of the template property."),
                        fieldWithPath("template_property_ids").description("A list of unknown template property ids."),
                    )
                )
            )
    }

    @Test
    fun missingPropertyValues() {
        documentedGetRequestTo(MissingPropertyValues(ThingId("R123"), ThingId("P123"), 5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_property_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing values for property "R123" with predicate "P123". min: "5", found: "2".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.min_value_count").value(5))
            .andExpect(jsonPath("$.actual_value_count").value(2))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("min_value_count").description("The minimum number of template property values."),
                        fieldWithPath("actual_value_count").description("The provided number of template property values."),
                    )
                )
            )
    }

    @Test
    fun tooManyPropertyValues() {
        documentedGetRequestTo(TooManyPropertyValues(ThingId("R123"), ThingId("P123"), 2, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_property_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for property "R123" with predicate "P123". max: "2", found: "5".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.max_value_count").value(2))
            .andExpect(jsonPath("$.actual_value_count").value(5))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("max_value_count").description("The maximum number of template property values."),
                        fieldWithPath("actual_value_count").description("The provided number of template property values."),
                    )
                )
            )
    }

    @Test
    fun invalidLiteral() {
        documentedGetRequestTo(InvalidLiteral(ThingId("R123"), ThingId("P123"), Classes.boolean, "#temp1", "0.15"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with value "0.15" for property "R123" with predicate "P123" is not a valid "${Classes.boolean}".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("0.15"))
            .andExpect(jsonPath("$.expected_datatype").value(Classes.boolean.value))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("object_label").description("The provided label of the object."),
                        fieldWithPath("expected_datatype").description("The enforced data type of the object."),
                    )
                )
            )
    }

    @Test
    fun mismatchedDataType() {
        documentedGetRequestTo(MismatchedDataType(ThingId("R123"), ThingId("P123"), "xsd:boolean", "#temp1", "String"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_data_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with data type "String" for property "R123" with predicate "P123" does not match expected data type "xsd:boolean".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.actual_datatype").value("String"))
            .andExpect(jsonPath("$.expected_datatype").value("xsd:boolean"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("actual_datatype").description("The provided data type of the object."),
                        fieldWithPath("expected_datatype").description("The expected data type of the object."),
                    )
                )
            )
    }

    @Test
    fun unrelatedTemplateProperty() {
        documentedGetRequestTo(UnrelatedTemplateProperty(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unrelated_template_property")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template property "R456" does not belong to template "R123".""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.template_property_id").value("R456"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_id").description("The id of the template."),
                        fieldWithPath("template_property_id").description("The id of the template property."),
                    )
                )
            )
    }

    @Test
    fun numberTooLow() {
        documentedGetRequestTo(NumberTooLow(ThingId("R123"), "#temp1", ThingId("P123"), "5", 10))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:number_too_low")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object "#temp1" for property "R123" with predicate "P123" must be at least "10".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("5"))
            .andExpect(jsonPath("$.min_inclusive").value(10))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("object_label").description("The provided label of the object."),
                        fieldWithPath("min_inclusive").description("The minimum value the object can have."),
                    )
                )
            )
    }

    @Test
    fun numberTooHigh() {
        documentedGetRequestTo(NumberTooHigh(ThingId("R123"), "#temp1", ThingId("P123"), "10", 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:number_too_high")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "10" for object "#temp1" for property "R123" with predicate "P123" must be at most "5".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("10"))
            .andExpect(jsonPath("$.max_inclusive").value(5))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_property_id").description("The id of the template property."),
                        fieldWithPath("predicate_id").description("The id of the predicate path of the template property."),
                        fieldWithPath("object_id").description("The id of the object."),
                        fieldWithPath("object_label").description("The provided label of the object."),
                        fieldWithPath("max_inclusive").description("The maximum value the object can have."),
                    )
                )
            )
    }

    @Test
    fun templateInstanceNotFound() {
        documentedGetRequestTo(TemplateInstanceNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_instance_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template instance for resource "R456" and template id "R123" not found.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.resource_id").value("R456"))
            .andDo(
                documentationHandler.document(
                    responseFields(exceptionResponseFields()).and(
                        fieldWithPath("template_id").description("The id of the template."),
                        fieldWithPath("resource_id").description("The id of the resource."),
                    )
                )
            )
    }
}
