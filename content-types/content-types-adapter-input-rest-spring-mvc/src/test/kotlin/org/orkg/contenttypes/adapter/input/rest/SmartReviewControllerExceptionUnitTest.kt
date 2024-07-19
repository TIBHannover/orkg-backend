package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.SmartReviewControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
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
internal class SmartReviewControllerExceptionUnitTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun ontologyEntityNotFound() {
        val entities = listOf("not", "found")

        get("/ontology-entity-not-found")
            .param("entities", *entities.toTypedArray())
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/ontology-entity-not-found"))
            .andExpect(jsonPath("$.message").value("""Ontology entity not found among entities "not", "found"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidSmartReviewTextSectionType() {
        val type = "comparison"

        get("/invalid-smart-review-text-section-type")
            .param("type", type)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/invalid-smart-review-text-section-type"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review text section type "$type"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun unrelatedSmartReviewSection() {
        val smartReviewId = "R123"
        val smartReviewSectionId = "R456"

        get("/unrelated-smart-review-section")
            .param("smartReviewId", smartReviewId)
            .param("smartReviewSectionId", smartReviewSectionId)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/unrelated-smart-review-section"))
            .andExpect(jsonPath("$.message").value("""Smart review section "$smartReviewSectionId" does not belong to smart review "$smartReviewId"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeComparisonSection() {
        get("/smart-review-section-type-mismatch-must-be-comparison-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-comparison-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be a comparison section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeVisualizationSection() {
        get("/smart-review-section-type-mismatch-must-be-visualization-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-visualization-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be a visualization section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeResourceSection() {
        get("/smart-review-section-type-mismatch-must-be-resource-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-resource-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be a resource section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBePredicateSection() {
        get("/smart-review-section-type-mismatch-must-be-predicate-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-predicate-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be a predicate section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeOntologySection() {
        get("/smart-review-section-type-mismatch-must-be-ontology-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-ontology-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be an ontology section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun smartReviewSectionTypeMismatchMustBeTextSection() {
        get("/smart-review-section-type-mismatch-must-be-text-section")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/smart-review-section-type-mismatch-must-be-text-section"))
            .andExpect(jsonPath("$.message").value("""Invalid smart review section type. Must be a text section."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/ontology-entity-not-found")
        fun ontologyEntityNotFound(@RequestParam entities: Set<ThingId>) {
            throw OntologyEntityNotFound(entities)
        }

        @GetMapping("/invalid-smart-review-text-section-type")
        fun invalidSmartReviewTextSectionType(@RequestParam type: ThingId) {
            throw InvalidSmartReviewTextSectionType(type)
        }

        @GetMapping("/unrelated-smart-review-section")
        fun unrelatedSmartReviewSection(
            @RequestParam smartReviewId: ThingId,
            @RequestParam smartReviewSectionId: ThingId
        ) {
            throw UnrelatedSmartReviewSection(smartReviewId, smartReviewSectionId)
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-comparison-section")
        fun smartReviewSectionTypeMismatchMustBeComparisonSection() {
            throw SmartReviewSectionTypeMismatch.mustBeComparisonSection()
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-visualization-section")
        fun smartReviewSectionTypeMismatchMustBeVisualizationSection() {
            throw SmartReviewSectionTypeMismatch.mustBeVisualizationSection()
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-resource-section")
        fun smartReviewSectionTypeMismatchMustBeResourceSection() {
            throw SmartReviewSectionTypeMismatch.mustBeResourceSection()
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-predicate-section")
        fun smartReviewSectionTypeMismatchMustBePredicateSection() {
            throw SmartReviewSectionTypeMismatch.mustBePredicateSection()
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-ontology-section")
        fun smartReviewSectionTypeMismatchMustBeOntologySection() {
            throw SmartReviewSectionTypeMismatch.mustBeOntologySection()
        }

        @GetMapping("/smart-review-section-type-mismatch-must-be-text-section")
        fun smartReviewSectionTypeMismatchMustBeTextSection() {
            throw SmartReviewSectionTypeMismatch.mustBeTextSection()
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
