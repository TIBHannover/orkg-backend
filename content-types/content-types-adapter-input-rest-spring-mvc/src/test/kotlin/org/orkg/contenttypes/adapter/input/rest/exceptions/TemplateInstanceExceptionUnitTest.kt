package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.thingIdConstraint
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
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerExceptionUnitTestConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.testing.spring.MockMvcExceptionBaseTest
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.exceptionResponseFields
import org.orkg.testing.spring.restdocs.type
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerExceptionUnitTestConfiguration::class])
internal class TemplateInstanceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateNotApplicable() {
        val type = "orkg:problem:template_not_applicable"
        documentedGetRequestTo(TemplateNotApplicable(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template "R123" cannot be applied to resource "R456" because the target resource is not an instance of the template target class.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.resource_id").value("R456"))
            .andDocument {
                responseFields<TemplateNotApplicable>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    fieldWithPath("resource_id").description("The id of the resource.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectIsNotAClass() {
        val type = "orkg:problem:object_is_not_a_class"
        documentedGetRequestTo(ObjectIsNotAClass(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a class.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDocument {
                responseFields<ObjectIsNotAClass>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectIsNotAPredicate() {
        val type = "orkg:problem:object_is_not_a_predicate"
        documentedGetRequestTo(ObjectIsNotAPredicate(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a predicate.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDocument {
                responseFields<ObjectIsNotAPredicate>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectIsNotAList() {
        val type = "orkg:problem:object_is_not_a_list"
        documentedGetRequestTo(ObjectIsNotAList(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a list.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDocument {
                responseFields<ObjectIsNotAList>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectIsNotALiteral() {
        val type = "orkg:problem:object_is_not_a_literal"
        documentedGetRequestTo(ObjectIsNotALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a literal.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDocument {
                responseFields<ObjectIsNotALiteral>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun objectMustNotBeALiteral() {
        val type = "orkg:problem:object_must_not_be_a_literal"
        documentedGetRequestTo(ObjectMustNotBeALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" must not be a literal.""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andDocument {
                responseFields<ObjectMustNotBeALiteral>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun resourceIsNotAnInstanceOfTargetClass() {
        val type = "orkg:problem:resource_is_not_an_instance_of_target_class"
        documentedGetRequestTo(ResourceIsNotAnInstanceOfTargetClass(ThingId("R123"), ThingId("P123"), "#temp1", ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not an instance of target class "C123".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.template_property_target_class").value("C123"))
            .andDocument {
                responseFields<ResourceIsNotAnInstanceOfTargetClass>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("template_property_target_class").description("The target class of the template property.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun labelDoesNotMatchPattern() {
        val type = "orkg:problem:label_does_not_match_pattern"
        documentedGetRequestTo(LabelDoesNotMatchPattern(ThingId("R123"), "#temp1", ThingId("P123"), "label", "\\d+"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Label "label" for object "#temp1" for property "R123" with predicate "P123" does not match pattern "\d+".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("label"))
            .andExpect(jsonPath("$.regex_pattern").value("\\d+"))
            .andDocument {
                responseFields<LabelDoesNotMatchPattern>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("object_label").description("The provided label of the object."),
                    fieldWithPath("regex_pattern").description("The regex pattern the object label has to match."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unknownTemplateProperties() {
        val type = "orkg:problem:unknown_template_properties"
        documentedGetRequestTo(UnknownTemplateProperties(ThingId("R123"), setOf(ThingId("R1"), ThingId("R2"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown properties for template "R123": "R1", "R2".""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.template_property_ids.length()").value("2"))
            .andExpect(jsonPath("$.template_property_ids[0]").value("R1"))
            .andExpect(jsonPath("$.template_property_ids[1]").value("R2"))
            .andDocument {
                responseFields<UnknownTemplateProperties>(
                    fieldWithPath("template_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("template_property_ids[]").description("A list of unknown template property ids.").arrayItemsType("string").constraints(thingIdConstraint),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun missingPropertyValues() {
        val type = "orkg:problem:missing_property_values"
        documentedGetRequestTo(MissingPropertyValues(ThingId("R123"), ThingId("P123"), 5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing values for property "R123" with predicate "P123". min: "5", found: "2".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.min_value_count").value(5))
            .andExpect(jsonPath("$.actual_value_count").value(2))
            .andDocument {
                responseFields<MissingPropertyValues>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("min_value_count").description("The minimum number of template property values.").type<Int>(),
                    fieldWithPath("actual_value_count").description("The provided number of template property values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun tooManyPropertyValues() {
        val type = "orkg:problem:too_many_property_values"
        documentedGetRequestTo(TooManyPropertyValues(ThingId("R123"), ThingId("P123"), 2, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for property "R123" with predicate "P123". max: "2", found: "5".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.max_value_count").value(2))
            .andExpect(jsonPath("$.actual_value_count").value(5))
            .andDocument {
                responseFields<TooManyPropertyValues>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("max_value_count").description("The maximum number of template property values.").type<Int>(),
                    fieldWithPath("actual_value_count").description("The provided number of template property values.").type<Int>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun invalidLiteral() {
        val type = "orkg:problem:invalid_literal"
        documentedGetRequestTo(InvalidLiteral(ThingId("R123"), ThingId("P123"), Classes.boolean, "#temp1", "0.15"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with value "0.15" for property "R123" with predicate "P123" is not a valid "${Classes.boolean}".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("0.15"))
            .andExpect(jsonPath("$.expected_datatype").value(Classes.boolean.value))
            .andDocument {
                responseFields<InvalidLiteral>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("object_label").description("The provided label of the object."),
                    fieldWithPath("expected_datatype").description("The enforced data type of the object.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun mismatchedDataType() {
        val type = "orkg:problem:mismatched_data_type"
        documentedGetRequestTo(MismatchedDataType(ThingId("R123"), ThingId("P123"), "xsd:boolean", "#temp1", "String"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with data type "String" for property "R123" with predicate "P123" does not match expected data type "xsd:boolean".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.actual_datatype").value("String"))
            .andExpect(jsonPath("$.expected_datatype").value("xsd:boolean"))
            .andDocument {
                responseFields<MismatchedDataType>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("actual_datatype").description("The provided data type of the object."),
                    fieldWithPath("expected_datatype").description("The expected data type of the object."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun unrelatedTemplateProperty() {
        val type = "orkg:problem:unrelated_template_property"
        documentedGetRequestTo(UnrelatedTemplateProperty(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template property "R456" does not belong to template "R123".""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.template_property_id").value("R456"))
            .andDocument {
                responseFields<UnrelatedTemplateProperty>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun numberTooLow() {
        val type = "orkg:problem:number_too_low"
        documentedGetRequestTo(NumberTooLow(ThingId("R123"), "#temp1", ThingId("P123"), "5", 10))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object "#temp1" for property "R123" with predicate "P123" must be at least "10".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("5"))
            .andExpect(jsonPath("$.min_inclusive").value(10))
            .andDocument {
                responseFields<NumberTooLow>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("object_label").description("The provided label of the object."),
                    fieldWithPath("min_inclusive").description("The minimum value the object can have."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun numberTooHigh() {
        val type = "orkg:problem:number_too_high"
        documentedGetRequestTo(NumberTooHigh(ThingId("R123"), "#temp1", ThingId("P123"), "10", 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType(type)
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "10" for object "#temp1" for property "R123" with predicate "P123" must be at most "5".""")
            .andExpect(jsonPath("$.template_property_id").value("R123"))
            .andExpect(jsonPath("$.predicate_id").value("P123"))
            .andExpect(jsonPath("$.object_id").value("#temp1"))
            .andExpect(jsonPath("$.object_label").value("10"))
            .andExpect(jsonPath("$.max_inclusive").value(5))
            .andDocument {
                responseFields<NumberTooHigh>(
                    fieldWithPath("template_property_id").description("The id of the template property.").type<ThingId>(),
                    fieldWithPath("predicate_id").description("The id of the predicate path of the template property.").type<ThingId>(),
                    fieldWithPath("object_id").description("The id of the object."),
                    fieldWithPath("object_label").description("The provided label of the object."),
                    fieldWithPath("max_inclusive").description("The maximum value the object can have."),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }

    @Test
    fun templateInstanceNotFound() {
        val type = "orkg:problem:template_instance_not_found"
        documentedGetRequestTo(TemplateInstanceNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType(type)
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template instance for resource "R456" and template id "R123" not found.""")
            .andExpect(jsonPath("$.template_id").value("R123"))
            .andExpect(jsonPath("$.resource_id").value("R456"))
            .andDocument {
                responseFields<TemplateInstanceNotFound>(
                    fieldWithPath("template_id").description("The id of the template.").type<ThingId>(),
                    fieldWithPath("resource_id").description("The id of the resource.").type<ThingId>(),
                    *exceptionResponseFields(type).toTypedArray(),
                )
            }
    }
}
