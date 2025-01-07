package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.ExceptionControllerUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedFigureNotModifiable
import org.orkg.contenttypes.domain.ComparisonRelatedResourceNotModifiable
import org.orkg.contenttypes.domain.InvalidBibTeXReference
import org.orkg.contenttypes.domain.InvalidBounds
import org.orkg.contenttypes.domain.InvalidDatatype
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.domain.InvalidLiteral
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.domain.InvalidSubjectPositionCardinality
import org.orkg.contenttypes.domain.InvalidSubjectPositionType
import org.orkg.contenttypes.domain.LabelDoesNotMatchPattern
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.MismatchedDataType
import org.orkg.contenttypes.domain.MissingPropertyPlaceholder
import org.orkg.contenttypes.domain.MissingPropertyValues
import org.orkg.contenttypes.domain.MissingSubjectPosition
import org.orkg.contenttypes.domain.NumberTooHigh
import org.orkg.contenttypes.domain.NumberTooLow
import org.orkg.contenttypes.domain.ObjectIsNotAClass
import org.orkg.contenttypes.domain.ObjectIsNotAList
import org.orkg.contenttypes.domain.ObjectIsNotALiteral
import org.orkg.contenttypes.domain.ObjectIsNotAPredicate
import org.orkg.contenttypes.domain.ObjectMustNotBeALiteral
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.ResourceIsNotAnInstanceOfTargetClass
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.contenttypes.domain.TemplateNotApplicable
import org.orkg.contenttypes.domain.TooManyPropertyValues
import org.orkg.contenttypes.domain.UnknownTemplateProperties
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
import org.orkg.contenttypes.domain.UnrelatedTemplateProperty
import org.orkg.graph.domain.Predicates
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class ExceptionControllerUnitTest : RestDocsTest("exceptions") {

    @Test
    fun invalidMonth() {
        get("/invalid-month")
            .param("month", "0")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-month"))
            .andExpect(jsonPath("$.message").value("""Invalid month "0". Must be in range [1..12]."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun sustainableDevelopmentGoalNotFound() {
        get("/sustainable-development-goal-not-found")
            .param("sdgId", "SDG1")
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/sustainable-development-goal-not-found"))
            .andExpect(jsonPath("$.message").value("""Sustainable Development Goal "SDG1" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literatureListNotFound() {
        val id = ThingId("R123")

        get("/errors/literature-list-not-found")
            .param("id", id.value)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/errors/literature-list-not-found"))
            .andExpect(jsonPath("$.message").value("""Literature list "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun templateNotApplicable() {
        val templateId = ThingId("R123")
        val id = ThingId("R456")

        get("/template-not-applicable")
            .param("templateId", templateId.value)
            .param("id", id.value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/template-not-applicable"))
            .andExpect(jsonPath("$.message").value("""Template "$templateId" cannot be applied to resource "$id" because the target resource is not an instance of the template target class."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun objectIsNotAClass() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"

        get("/object-is-not-a-class")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/object-is-not-a-class"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a class."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun objectIsNotAPredicate() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"

        get("/object-is-not-a-predicate")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/object-is-not-a-predicate"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a predicate."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun objectIsNotAList() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"

        get("/object-is-not-a-list")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/object-is-not-a-list"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a list."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun objectIsNotALiteral() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"

        get("/object-is-not-a-literal")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/object-is-not-a-literal"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not a literal."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun objectMustNotBeALiteral() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"

        get("/object-must-not-be-a-literal")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/object-must-not-be-a-literal"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" must not be a literal."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun resourceIsNotAnInstanceOfTargetClass() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val id = "#temp1"
        val targetClass = "C123"

        get("/resource-is-not-an-instance-of-target-class")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("id", id)
            .param("targetClass", targetClass)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/resource-is-not-an-instance-of-target-class"))
            .andExpect(jsonPath("$.message").value("""Object "$id" for template property "$templatePropertyId" with predicate "$predicateId" is not an instance of target class "$targetClass"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun labelDoesNotMatchPattern() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val objectId = "#temp1"
        val pattern = "\\d+"
        val label = "label"

        get("/label-does-not-match-pattern")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("objectId", objectId)
            .param("pattern", pattern)
            .param("label", label)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/label-does-not-match-pattern"))
            .andExpect(jsonPath("$.message").value("""Label "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" does not match pattern "$pattern"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun unknownTemplateProperties() {
        val templateId = "R123"
        val unknownProperties = arrayOf("R1", "R2")

        get("/unknown-template-properties")
            .param("templateId", templateId)
            .param("unknownProperties", *unknownProperties)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/unknown-template-properties"))
            .andExpect(jsonPath("$.message").value("""Unknown properties for template "$templateId": ${unknownProperties.joinToString { "\"$it\"" } }."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingPropertyValues() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val min = 5
        val actual = 2

        get("/missing-property-values")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("min", min.toString())
            .param("actual", actual.toString())
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-property-values"))
            .andExpect(jsonPath("$.message").value("""Missing values for property "$templatePropertyId" with predicate "$predicateId". min: "$min", found: "$actual"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tooManyPropertyValues() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val max = 2
        val actual = 5

        get("/too-many-property-values")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("max", max.toString())
            .param("actual", actual.toString())
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/too-many-property-values"))
            .andExpect(jsonPath("$.message").value("""Too many values for property "$templatePropertyId" with predicate "$predicateId". max: "$max", found: "$actual"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLiteral() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val datatype = "Boolean"
        val objectId = "#temp1"
        val value = "0.15"

        get("/invalid-literal")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("datatype", datatype)
            .param("id", objectId)
            .param("value", value)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-literal"))
            .andExpect(jsonPath("$.message").value("""Object "$objectId" with value "$value" for property "$templatePropertyId" with predicate "$predicateId" is not a valid "$datatype"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun mismatchedDataType() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val expectedDataType = "xsd:boolean"
        val id = "#temp1"
        val foundDataType = "String"

        get("/mismatched-data-type")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("expectedDataType", expectedDataType)
            .param("id", id)
            .param("foundDataType", foundDataType)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/mismatched-data-type"))
            .andExpect(jsonPath("$.message").value("""Object "$id" with data type "$foundDataType" for property "$templatePropertyId" with predicate "$predicateId" does not match expected data type "$expectedDataType"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun unrelatedTemplateProperty() {
        val templateId = "R123"
        val templatePropertyId = "R456"

        get("/unrelated-template-property")
            .param("templateId", templateId)
            .param("templatePropertyId", templatePropertyId)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/unrelated-template-property"))
            .andExpect(jsonPath("$.message").value("""Template property "$templatePropertyId" does not belong to template "$templateId"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidBounds() {
        val min = "5"
        val max = "4"

        get("/invalid-bounds")
            .param("min", min)
            .param("max", max)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-bounds"))
            .andExpect(jsonPath("$.message").value("""Invalid bounds. Min bound must be less than or equal to max bound. Found: min: "$min", max: "$max"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDatatype() {
        val actual = "int"
        val expected = "string"

        get("/invalid-datatype")
            .param("actual", actual)
            .param("expected", expected)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-datatype"))
            .andExpect(jsonPath("$.message").value("""Invalid datatype. Found "$actual", expected "$expected"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDatatypeWithSeveralPossibleTypes() {
        val actual = "int"
        val expected1 = "string"
        val expected2 = "date"
        val expected3 = "uri"
        val expected4 = "float"

        get("/invalid-datatype-several")
            .param("actual", actual)
            .param("expected", expected1, expected2, expected3, expected4)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-datatype-several"))
            .andExpect(jsonPath("$.message").value("""Invalid datatype. Found "$actual", expected either of "$expected1", "$expected2", "$expected3", "$expected4"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun numberTooLow() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val objectId = "#temp1"
        val minInclusive = "10"
        val label = "5"

        get("/number-too-low")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("objectId", objectId)
            .param("minInclusive", minInclusive)
            .param("label", label)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/number-too-low"))
            .andExpect(jsonPath("$.message").value("""Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at least "$minInclusive"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun numberTooHigh() {
        val templatePropertyId = "R123"
        val predicateId = "P123"
        val objectId = "#temp1"
        val maxInclusive = "10"
        val label = "5"

        get("/number-too-high")
            .param("templatePropertyId", templatePropertyId)
            .param("predicateId", predicateId)
            .param("objectId", objectId)
            .param("maxInclusive", maxInclusive)
            .param("label", label)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/number-too-high"))
            .andExpect(jsonPath("$.message").value("""Number "$label" for object "$objectId" for property "$templatePropertyId" with predicate "$predicateId" must be at most "$maxInclusive"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun paperNotModifiable() {
        val id = "R123"

        get("/paper-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/paper-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Paper "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonNotModifiable() {
        val id = "R123"

        get("/comparison-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonRelatedResourceNotModifiable() {
        val id = "R123"

        get("/comparison-related-resource-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-related-resource-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison related resource "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun comparisonRelatedFigureNotModifiable() {
        val id = "R123"

        get("/comparison-related-figure-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/comparison-related-figure-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Comparison related figure "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literatureListNotModifiable() {
        val id = "R123"

        get("/literature-list-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/literature-list-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Literature list "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewNotModifiable() {
        val id = "R123"

        get("/smart-review-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/smart-review-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Smart review "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidListSectionEntry() {
        val id = "R123"
        val expectedAnyInstanceOf = arrayOf("C1", "C2")

        get("/invalid-list-section-entry")
            .param("id", id)
            .param("expectedAnyInstanceOf", *expectedAnyInstanceOf)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-list-section-entry"))
            .andExpect(jsonPath("$.message").value("""Invalid list section entry "$id". Must be an instance of either "C1", "C2"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidHeadingSize() {
        val headingSize = "5"

        get("/invalid-heading-size")
            .param("headingSize", headingSize)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-heading-size"))
            .andExpect(jsonPath("$.message").value("""Invalid heading size "$headingSize". Must be at least 1."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun unrelatedLiteratureListSection() {
        val literatureListId = "R123"
        val literatureListSectionId = "R456"

        get("/unrelated-literature-list-section")
            .param("literatureListId", literatureListId)
            .param("literatureListSectionId", literatureListSectionId)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/unrelated-literature-list-section"))
            .andExpect(jsonPath("$.message").value("""Literature list section "$literatureListSectionId" does not belong to literature list "$literatureListId"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literatureListSectionTypeMismatchMustBeTextSection() {
        get("/literature-list-section-type-mismatch-must-be-text-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/literature-list-section-type-mismatch-must-be-text-section"))
            .andExpect(jsonPath("$.message").value("""Invalid literature list section type. Must be a text section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literatureListSectionTypeMismatchMustBeListSection() {
        get("/literature-list-section-type-mismatch-must-be-list-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/literature-list-section-type-mismatch-must-be-list-section"))
            .andExpect(jsonPath("$.message").value("""Invalid literature list section type. Must be a list section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidSubjectPositionCardinality() {
        get("/invalid-subject-position-cardinality")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-subject-position-cardinality"))
            .andExpect(jsonPath("$.message").value("""Invalid subject position cardinality. Minimum cardinality must be at least one."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidSubjectPositionType() {
        get("/invalid-subject-position-type")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-subject-position-type"))
            .andExpect(jsonPath("$.message").value("""Invalid subject position type. Subject position must not be a literal property."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingSubjectPosition() {
        get("/missing-subject-position")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-subject-position"))
            .andExpect(jsonPath("$.message").value("""Missing subject position. There must be at least one property with path "${Predicates.hasSubjectPosition}" that has a minimum cardinality of at least one."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingPropertyPlaceholder() {
        val index = "4"

        get("/missing-property-placeholder")
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-property-placeholder"))
            .andExpect(jsonPath("$.message").value("""Missing placeholder for property at index "$index"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidBibTeXReference() {
        val reference = "not bibtex"

        get("/invalid-bibtex-reference")
            .param("reference", reference)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-bibtex-reference"))
            .andExpect(jsonPath("$.message").value("""Invalid BibTeX reference "$reference"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/invalid-month")
        fun invalidMonth(@RequestParam month: Int) {
            throw InvalidMonth(month)
        }

        @GetMapping("/sustainable-development-goal-not-found")
        fun sustainableDevelopmentGoalNotFound(@RequestParam sdgId: ThingId) {
            throw SustainableDevelopmentGoalNotFound(sdgId)
        }

        @GetMapping("/errors/literature-list-not-found")
        fun literatureListNotFound(@RequestParam id: ThingId) {
            throw LiteratureListNotFound(id)
        }

        @GetMapping("/template-not-applicable")
        fun templateNotApplicable(@RequestParam templateId: ThingId, @RequestParam id: ThingId) {
            throw TemplateNotApplicable(templateId, id)
        }

        @GetMapping("/object-is-not-a-class")
        fun objectIsNotAClass(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String
        ) {
            throw ObjectIsNotAClass(templatePropertyId, predicateId, id)
        }

        @GetMapping("/object-is-not-a-predicate")
        fun objectIsNotAPredicate(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String
        ) {
            throw ObjectIsNotAPredicate(templatePropertyId, predicateId, id)
        }

        @GetMapping("/object-is-not-a-list")
        fun objectIsNotAList(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String
        ) {
            throw ObjectIsNotAList(templatePropertyId, predicateId, id)
        }

        @GetMapping("/object-is-not-a-literal")
        fun objectIsNotALiteral(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String
        ) {
            throw ObjectIsNotALiteral(templatePropertyId, predicateId, id)
        }

        @GetMapping("/object-must-not-be-a-literal")
        fun objectMustNotBeALiteral(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String
        ) {
            throw ObjectMustNotBeALiteral(templatePropertyId, predicateId, id)
        }

        @GetMapping("/resource-is-not-an-instance-of-target-class")
        fun resourceIsNotAnInstanceOfTargetClass(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam id: String,
            @RequestParam targetClass: ThingId
        ) {
            throw ResourceIsNotAnInstanceOfTargetClass(templatePropertyId, predicateId, id, targetClass)
        }

        @GetMapping("/label-does-not-match-pattern")
        fun labelDoesNotMatchPattern(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam objectId: String,
            @RequestParam predicateId: ThingId,
            @RequestParam label: String,
            @RequestParam pattern: String
        ) {
            throw LabelDoesNotMatchPattern(templatePropertyId, objectId, predicateId, label, pattern)
        }

        @GetMapping("/unknown-template-properties")
        fun unknownTemplateProperties(
            @RequestParam templateId: ThingId,
            @RequestParam("unknownProperties") unknownProperties: Set<ThingId>
        ) {
            throw UnknownTemplateProperties(templateId, unknownProperties)
        }

        @GetMapping("/missing-property-values")
        fun missingPropertyValues(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam min: Int,
            @RequestParam actual: Int
        ) {
            throw MissingPropertyValues(templatePropertyId, predicateId, min, actual)
        }

        @GetMapping("/too-many-property-values")
        fun tooManyPropertyValues(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam max: Int,
            @RequestParam actual: Int
        ) {
            throw TooManyPropertyValues(templatePropertyId, predicateId, max, actual)
        }

        @GetMapping("/invalid-literal")
        fun invalidLiteral(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam datatype: ThingId,
            @RequestParam id: String,
            @RequestParam value: String
        ) {
            throw InvalidLiteral(templatePropertyId, predicateId, datatype, id, value)
        }

        @GetMapping("/mismatched-data-type")
        fun mismatchedDataType(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam predicateId: ThingId,
            @RequestParam expectedDataType: String,
            @RequestParam id: String,
            @RequestParam foundDataType: String
        ) {
            throw MismatchedDataType(templatePropertyId, predicateId, expectedDataType, id, foundDataType)
        }

        @GetMapping("/unrelated-template-property")
        fun invalidLiteral(
            @RequestParam templateId: ThingId,
            @RequestParam templatePropertyId: ThingId,
        ) {
            throw UnrelatedTemplateProperty(templateId, templatePropertyId)
        }

        @GetMapping("/invalid-bounds")
        fun invalidBounds(
            @RequestParam min: Number,
            @RequestParam max: Number,
        ) {
            throw InvalidBounds(min, max)
        }

        @GetMapping("/invalid-datatype")
        fun invalidDatatype(
            @RequestParam actual: ThingId,
            @RequestParam expected: ThingId,
        ) {
            throw InvalidDatatype(actual, expected)
        }

        @GetMapping("/invalid-datatype-several")
        fun invalidDatatype(
            @RequestParam actual: ThingId,
            @RequestParam expected: List<ThingId>,
        ) {
            throw InvalidDatatype(actual, expected[0], expected[1], *expected.drop(2).toTypedArray())
        }

        @GetMapping("/number-too-low")
        fun numberTooLow(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam objectId: String,
            @RequestParam predicateId: ThingId,
            @RequestParam label: String,
            @RequestParam minInclusive: Number
        ) {
            throw NumberTooLow(templatePropertyId, objectId, predicateId, label, minInclusive)
        }

        @GetMapping("/number-too-high")
        fun numberTooHigh(
            @RequestParam templatePropertyId: ThingId,
            @RequestParam objectId: String,
            @RequestParam predicateId: ThingId,
            @RequestParam label: String,
            @RequestParam maxInclusive: Number
        ) {
            throw NumberTooHigh(templatePropertyId, objectId, predicateId, label, maxInclusive)
        }

        @GetMapping("/paper-not-modifiable")
        fun paperNotModifiable(@RequestParam id: ThingId) {
            throw PaperNotModifiable(id)
        }

        @GetMapping("/comparison-not-modifiable")
        fun comparisonNotModifiable(@RequestParam id: ThingId) {
            throw ComparisonNotModifiable(id)
        }

        @GetMapping("/comparison-related-resource-not-modifiable")
        fun comparisonRelatedResourceNotModifiable(@RequestParam id: ThingId) {
            throw ComparisonRelatedResourceNotModifiable(id)
        }

        @GetMapping("/comparison-related-figure-not-modifiable")
        fun comparisonRelatedFigureNotModifiable(@RequestParam id: ThingId) {
            throw ComparisonRelatedFigureNotModifiable(id)
        }

        @GetMapping("/literature-list-not-modifiable")
        fun literatureListNotModifiable(@RequestParam id: ThingId) {
            throw LiteratureListNotModifiable(id)
        }

        @GetMapping("/smart-review-not-modifiable")
        fun smartReviewNotModifiable(@RequestParam id: ThingId) {
            throw SmartReviewNotModifiable(id)
        }

        @GetMapping("/invalid-list-section-entry")
        fun invalidListSectionEntry(@RequestParam id: ThingId, @RequestParam expectedAnyInstanceOf: Set<ThingId>) {
            throw InvalidListSectionEntry(id, expectedAnyInstanceOf)
        }

        @GetMapping("/invalid-heading-size")
        fun invalidHeadingSize(@RequestParam headingSize: Int) {
            throw InvalidHeadingSize(headingSize)
        }

        @GetMapping("/unrelated-literature-list-section")
        fun unrelatedLiteratureListSection(
            @RequestParam literatureListId: ThingId,
            @RequestParam literatureListSectionId: ThingId
        ) {
            throw UnrelatedLiteratureListSection(literatureListId, literatureListSectionId)
        }

        @GetMapping("/literature-list-section-type-mismatch-must-be-text-section")
        fun literatureListSectionTypeMismatchMustBeTextSection() {
            throw LiteratureListSectionTypeMismatch.mustBeTextSection()
        }

        @GetMapping("/literature-list-section-type-mismatch-must-be-list-section")
        fun literatureListSectionTypeMismatchMustBeListSection() {
            throw LiteratureListSectionTypeMismatch.mustBeListSection()
        }

        @GetMapping("/invalid-subject-position-cardinality")
        fun invalidSubjectPositionCardinality() {
            throw InvalidSubjectPositionCardinality()
        }

        @GetMapping("/invalid-subject-position-type")
        fun invalidSubjectPositionType() {
            throw InvalidSubjectPositionType()
        }

        @GetMapping("/missing-subject-position")
        fun missingSubjectPosition() {
            throw MissingSubjectPosition()
        }

        @GetMapping("/missing-property-placeholder")
        fun missingPropertyPlaceholder(@RequestParam index: Int) {
            throw MissingPropertyPlaceholder(index)
        }

        @GetMapping("/invalid-bibtex-reference")
        fun invalidBibTeXReference(@RequestParam reference: String) {
            throw InvalidBibTeXReference(reference)
        }
    }
}
