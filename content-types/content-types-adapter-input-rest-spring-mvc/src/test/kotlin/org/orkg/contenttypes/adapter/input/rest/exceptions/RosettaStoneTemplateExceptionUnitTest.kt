package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.RosettaStoneTemplateExceptionUnitTest.TestController
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
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@WebMvcTest
@ContextConfiguration(classes = [TestController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class RosettaStoneTemplateExceptionUnitTest : MockMvcBaseTest("rosetta-stone-templates") {
    @Test
    fun invalidSubjectPositionPath() {
        get("/invalid-subject-position-path")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-subject-position-path"))
            .andExpect(jsonPath("$.message").value("""Invalid subject position path. Must be "${Predicates.hasSubjectPosition}"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidObjectPositionPath() {
        val index = "5"

        get("/invalid-object-position-path")
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-object-position-path"))
            .andExpect(jsonPath("$.message").value("""Invalid object position path for property at index "$index". Must be "${Predicates.hasObjectPosition}"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateNotModifiable() {
        val id = "R123"

        get("/rosetta-stone-template-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Rosetta stone template "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplatePropertyNotModifiable() {
        val id = "R123"

        get("/rosetta-stone-template-property-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-property-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Rosetta stone template property "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateInUseCantBeDeleted() {
        val id = "R123"

        get("/rosetta-stone-template-in-use-cant-be-deleted")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-in-use-cant-be-deleted"))
            .andExpect(jsonPath("$.message").value("""Unable to delete rosetta stone template "$id" because it is used in at least one (rosetta stone) statement."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateInUseCantUpdateProperty() {
        val id = "R123"
        val property = "abc"

        get("/rosetta-stone-template-in-use-cant-update-property")
            .param("id", id)
            .param("property", property)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-in-use-cant-update-property"))
            .andExpect(jsonPath("$.message").value("""Unable to update $property of rosetta stone template "$id" because it is used in at least one rosetta stone statement."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingFormattedLabelPlaceholderIndex() {
        val index = "4"

        get("/missing-formatted-label-placeholder-index")
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-formatted-label-placeholder-index"))
            .andExpect(jsonPath("$.message").value("""Missing formatted label placeholder "{$index}"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingFormattedLabelPlaceholderPlaceholder() {
        val placeholder = "4"

        get("/missing-formatted-label-placeholder-placeholder")
            .param("placeholder", placeholder)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-formatted-label-placeholder-placeholder"))
            .andExpect(jsonPath("$.message").value("""Missing formatted label placeholder for input position "$placeholder"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateLabelMustStartWithPreviousVersion() {
        get("/rosetta-stone-template-label-must-start-with-previous-version")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-label-must-start-with-previous-version"))
            .andExpect(jsonPath("$.message").value("""The updated formatted label must start with the previous label."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun tooManyNewRosettaStoneTemplateLabelSections() {
        get("/too-many-new-rosetta-stone-template-label-sections")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/too-many-new-rosetta-stone-template-label-sections"))
            .andExpect(jsonPath("$.message").value("""Too many new formatted label sections. Must be exactly one optional section per new template property."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties() {
        get("/rosetta-stone-template-label-update-requires-new-template-properties")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-label-update-requires-new-template-properties"))
            .andExpect(jsonPath("$.message").value("""The formatted label can only be updated in combination with the addition of new template properties."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun newRosettaStoneTemplateLabelSectionsMustBeOptional() {
        get("/new-rosetta-stone-template-label-sections-must-be-optional")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/new-rosetta-stone-template-label-sections-must-be-optional"))
            .andExpect(jsonPath("$.message").value("""New sections of the formatted label must be optional."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneTemplateLabelMustBeUpdated() {
        get("/rosetta-stone-template-label-must-be-updated")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-label-must-be-updated"))
            .andExpect(jsonPath("$.message").value("""The formatted label must be updated when updating template properties."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun newRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage() {
        get("/new-rosetta-stone-template-example-usage-must-start-with-previous-example-usage")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/new-rosetta-stone-template-example-usage-must-start-with-previous-example-usage"))
            .andExpect(jsonPath("$.message").value("""New example usage must start with the previous example usage."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun newRosettaStoneTemplatePropertyMustBeOptional() {
        val placeholder = "4"

        get("/new-rosetta-stone-template-property-must-be-optional")
            .param("placeholder", placeholder)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/new-rosetta-stone-template-property-must-be-optional"))
            .andExpect(jsonPath("$.message").value("""New rosetta stone template property "$placeholder" must be optional."""))
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

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/invalid-subject-position-path")
        fun invalidSubjectPositionPath(): Unit = throw InvalidSubjectPositionPath()

        @GetMapping("/invalid-object-position-path")
        fun invalidObjectPositionPath(
            @RequestParam index: Int,
        ): Unit = throw InvalidObjectPositionPath(index)

        @GetMapping("/rosetta-stone-template-not-modifiable")
        fun rosettaStoneTemplateNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw RosettaStoneTemplateNotModifiable(id)

        @GetMapping("/rosetta-stone-template-property-not-modifiable")
        fun rosettaStoneTemplatePropertyNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw RosettaStoneTemplatePropertyNotModifiable(id)

        @GetMapping("/rosetta-stone-template-in-use-cant-be-deleted")
        fun rosettaStoneTemplateInUseCantBeDeleted(
            @RequestParam id: ThingId,
        ): Unit = throw RosettaStoneTemplateInUse.cantBeDeleted(id)

        @GetMapping("/rosetta-stone-template-in-use-cant-update-property")
        fun rosettaStoneTemplateInUseCantUpdateProperty(
            @RequestParam id: ThingId,
            @RequestParam property: String,
        ): Unit = throw RosettaStoneTemplateInUse.cantUpdateProperty(id, property)

        @GetMapping("/missing-formatted-label-placeholder-index")
        fun missingFormattedLabelPlaceholderIndex(
            @RequestParam index: Int,
        ): Unit = throw MissingFormattedLabelPlaceholder(index)

        @GetMapping("/missing-formatted-label-placeholder-placeholder")
        fun missingFormattedLabelPlaceholderPlaceholder(
            @RequestParam placeholder: String,
        ): Unit = throw MissingFormattedLabelPlaceholder(placeholder)

        @GetMapping("/rosetta-stone-template-label-must-start-with-previous-version")
        fun rosettaStoneTemplateLabelMustStartWithPreviousVersion(): Unit = throw RosettaStoneTemplateLabelMustStartWithPreviousVersion()

        @GetMapping("/too-many-new-rosetta-stone-template-label-sections")
        fun tooManyNewRosettaStoneTemplateLabelSections(): Unit = throw TooManyNewRosettaStoneTemplateLabelSections()

        @GetMapping("/rosetta-stone-template-label-update-requires-new-template-properties")
        fun rosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties(): Unit = throw RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties()

        @GetMapping("/new-rosetta-stone-template-label-sections-must-be-optional")
        fun newRosettaStoneTemplateLabelSectionsMustBeOptional(): Unit = throw NewRosettaStoneTemplateLabelSectionsMustBeOptional()

        @GetMapping("/rosetta-stone-template-label-must-be-updated")
        fun rosettaStoneTemplateLabelMustBeUpdated(): Unit = throw RosettaStoneTemplateLabelMustBeUpdated()

        @GetMapping("/new-rosetta-stone-template-example-usage-must-start-with-previous-example-usage")
        fun newRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage(): Unit = throw NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage()

        @GetMapping("/new-rosetta-stone-template-property-must-be-optional")
        fun newRosettaStoneTemplatePropertyMustBeOptional(
            @RequestParam placeholder: String,
        ): Unit = throw NewRosettaStoneTemplatePropertyMustBeOptional(placeholder)

        @GetMapping("/invalid-subject-position-cardinality")
        fun invalidSubjectPositionCardinality(): Unit = throw InvalidSubjectPositionCardinality()

        @GetMapping("/invalid-subject-position-type")
        fun invalidSubjectPositionType(): Unit = throw InvalidSubjectPositionType()

        @GetMapping("/missing-subject-position")
        fun missingSubjectPosition(): Unit = throw MissingSubjectPosition()

        @GetMapping("/missing-property-placeholder")
        fun missingPropertyPlaceholder(
            @RequestParam index: Int,
        ): Unit = throw MissingPropertyPlaceholder(index)
    }
}
