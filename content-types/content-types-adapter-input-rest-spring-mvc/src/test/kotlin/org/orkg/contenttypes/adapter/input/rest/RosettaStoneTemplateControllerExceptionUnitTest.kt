package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneTemplateControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.InvalidObjectPositionPath
import org.orkg.contenttypes.domain.InvalidSubjectPositionPath
import org.orkg.contenttypes.domain.MissingFormattedLabelPlaceholder
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
import org.orkg.testing.FixedClockConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class RosettaStoneTemplateControllerExceptionUnitTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

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
            .andExpect(jsonPath("$.message").value("""The updated formmated label must start with the previous label."""))
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

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/invalid-subject-position-path")
        fun invalidSubjectPositionPath() {
            throw InvalidSubjectPositionPath()
        }

        @GetMapping("/invalid-object-position-path")
        fun invalidObjectPositionPath(@RequestParam index: Int) {
            throw InvalidObjectPositionPath(index)
        }

        @GetMapping("/rosetta-stone-template-not-modifiable")
        fun rosettaStoneTemplateNotModifiable(@RequestParam id: ThingId) {
            throw RosettaStoneTemplateNotModifiable(id)
        }

        @GetMapping("/rosetta-stone-template-property-not-modifiable")
        fun rosettaStoneTemplatePropertyNotModifiable(@RequestParam id: ThingId) {
            throw RosettaStoneTemplatePropertyNotModifiable(id)
        }

        @GetMapping("/rosetta-stone-template-in-use-cant-be-deleted")
        fun rosettaStoneTemplateInUseCantBeDeleted(@RequestParam id: ThingId) {
            throw RosettaStoneTemplateInUse.cantBeDeleted(id)
        }

        @GetMapping("/rosetta-stone-template-in-use-cant-update-property")
        fun rosettaStoneTemplateInUseCantUpdateProperty(@RequestParam id: ThingId, @RequestParam property: String) {
            throw RosettaStoneTemplateInUse.cantUpdateProperty(id, property)
        }

        @GetMapping("/missing-formatted-label-placeholder-index")
        fun missingFormattedLabelPlaceholderIndex(@RequestParam index: Int) {
            throw MissingFormattedLabelPlaceholder(index)
        }

        @GetMapping("/missing-formatted-label-placeholder-placeholder")
        fun missingFormattedLabelPlaceholderPlaceholder(@RequestParam placeholder: String) {
            throw MissingFormattedLabelPlaceholder(placeholder)
        }

        @GetMapping("/rosetta-stone-template-label-must-start-with-previous-version")
        fun rosettaStoneTemplateLabelMustStartWithPreviousVersion() {
            throw RosettaStoneTemplateLabelMustStartWithPreviousVersion()
        }

        @GetMapping("/too-many-new-rosetta-stone-template-label-sections")
        fun tooManyNewRosettaStoneTemplateLabelSections() {
            throw TooManyNewRosettaStoneTemplateLabelSections()
        }

        @GetMapping("/rosetta-stone-template-label-update-requires-new-template-properties")
        fun rosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties() {
            throw RosettaStoneTemplateLabelUpdateRequiresNewTemplateProperties()
        }

        @GetMapping("/new-rosetta-stone-template-label-sections-must-be-optional")
        fun newRosettaStoneTemplateLabelSectionsMustBeOptional() {
            throw NewRosettaStoneTemplateLabelSectionsMustBeOptional()
        }

        @GetMapping("/rosetta-stone-template-label-must-be-updated")
        fun rosettaStoneTemplateLabelMustBeUpdated() {
            throw RosettaStoneTemplateLabelMustBeUpdated()
        }

        @GetMapping("/new-rosetta-stone-template-example-usage-must-start-with-previous-example-usage")
        fun newRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage() {
            throw NewRosettaStoneTemplateExampleUsageMustStartWithPreviousExampleUsage()
        }

        @GetMapping("/new-rosetta-stone-template-property-must-be-optional")
        fun newRosettaStoneTemplatePropertyMustBeOptional(@RequestParam placeholder: String) {
            throw NewRosettaStoneTemplatePropertyMustBeOptional(placeholder)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
