package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidDataType
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ContextConfiguration

@WebMvcTest
@ContextConfiguration(classes = [FixedClockConfig::class])
internal class TemplateInstanceExceptionUnitTest : MockMvcExceptionBaseTest() {
    @Test
    fun templateNotApplicable() {
        documentedGetRequestTo(TemplateNotApplicable(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:template_not_applicable")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template "R123" cannot be applied to resource "R456" because the target resource is not an instance of the template target class.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectIsNotAClass() {
        documentedGetRequestTo(ObjectIsNotAClass(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a class.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectIsNotAPredicate() {
        documentedGetRequestTo(ObjectIsNotAPredicate(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_predicate")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a predicate.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectIsNotAList() {
        documentedGetRequestTo(ObjectIsNotAList(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_list")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a list.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectIsNotALiteral() {
        documentedGetRequestTo(ObjectIsNotALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_is_not_a_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not a literal.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun objectMustNotBeALiteral() {
        documentedGetRequestTo(ObjectMustNotBeALiteral(ThingId("R123"), ThingId("P123"), "#temp1"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:object_must_not_be_a_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" must not be a literal.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun resourceIsNotAnInstanceOfTargetClass() {
        documentedGetRequestTo(ResourceIsNotAnInstanceOfTargetClass(ThingId("R123"), ThingId("P123"), "#temp1", ThingId("C123")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:resource_is_not_an_instance_of_target_class")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" for template property "R123" with predicate "P123" is not an instance of target class "C123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun labelDoesNotMatchPattern() {
        documentedGetRequestTo(LabelDoesNotMatchPattern(ThingId("R123"), "#temp1", ThingId("P123"), "label", "\\d+"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:label_does_not_match_pattern")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Label "label" for object "#temp1" for property "R123" with predicate "P123" does not match pattern "\d+".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unknownTemplateProperties() {
        documentedGetRequestTo(UnknownTemplateProperties(ThingId("R123"), setOf(ThingId("R1"), ThingId("R2"))))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_template_properties")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Unknown properties for template "R123": "R1", "R2".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun missingPropertyValues() {
        documentedGetRequestTo(MissingPropertyValues(ThingId("R123"), ThingId("P123"), 5, 2))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:missing_property_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Missing values for property "R123" with predicate "P123". min: "5", found: "2".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun tooManyPropertyValues() {
        documentedGetRequestTo(TooManyPropertyValues(ThingId("R123"), ThingId("P123"), 2, 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:too_many_property_values")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Too many values for property "R123" with predicate "P123". max: "2", found: "5".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidLiteral() {
        documentedGetRequestTo(InvalidLiteral(ThingId("R123"), ThingId("P123"), Classes.boolean, "#temp1", "0.15"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_literal")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with value "0.15" for property "R123" with predicate "P123" is not a valid "${Classes.boolean}".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun mismatchedDataType() {
        documentedGetRequestTo(MismatchedDataType(ThingId("R123"), ThingId("P123"), "xsd:boolean", "#temp1", "String"))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:mismatched_data_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Object "#temp1" with data type "String" for property "R123" with predicate "P123" does not match expected data type "xsd:boolean".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun unrelatedTemplateProperty() {
        documentedGetRequestTo(UnrelatedTemplateProperty(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unrelated_template_property")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Template property "R456" does not belong to template "R123".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidBounds() {
        documentedGetRequestTo(InvalidBounds(5, 4))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_bounds")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "5", max: "4".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidDatatype() {
        documentedGetRequestTo(InvalidDataType(ThingId("int"), ThingId("string")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_data_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid datatype. Found "int", expected "string".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun invalidDatatypeWithSeveralPossibleTypes() {
        get(InvalidDataType(ThingId("int"), ThingId("string"), ThingId("date"), ThingId("uri"), ThingId("float")))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_data_type")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Invalid datatype. Found "int", expected either of "string", "date", "uri", "float".""")
    }

    @Test
    fun numberTooLow() {
        documentedGetRequestTo(NumberTooLow(ThingId("R123"), "#temp1", ThingId("P123"), "5", 10))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:number_too_low")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "5" for object "#temp1" for property "R123" with predicate "P123" must be at least "10".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun numberTooHigh() {
        documentedGetRequestTo(NumberTooHigh(ThingId("R123"), "#temp1", ThingId("P123"), "10", 5))
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:number_too_high")
            .andExpectTitle("Bad Request")
            .andExpectDetail("""Number "10" for object "#temp1" for property "R123" with predicate "P123" must be at most "5".""")
            .andDocumentWithDefaultExceptionResponseFields()
    }

    @Test
    fun templateInstanceNotFound() {
        documentedGetRequestTo(TemplateInstanceNotFound(ThingId("R123"), ThingId("R456")))
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:template_instance_not_found")
            .andExpectTitle("Not Found")
            .andExpectDetail("""Template instance for resource "R456" and template id "R123" not found.""")
            .andDocumentWithDefaultExceptionResponseFields()
    }
}
