package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.SmartReviewExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.InvalidSmartReviewTextSectionType
import org.orkg.contenttypes.domain.OntologyEntityNotFound
import org.orkg.contenttypes.domain.PublishedSmartReviewContentNotFound
import org.orkg.contenttypes.domain.SmartReviewAlreadyPublished
import org.orkg.contenttypes.domain.SmartReviewNotModifiable
import org.orkg.contenttypes.domain.SmartReviewSectionTypeMismatch
import org.orkg.contenttypes.domain.UnrelatedSmartReviewSection
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
internal class SmartReviewExceptionUnitTest : MockMvcBaseTest("smart-reviews") {
    @Test
    fun publishedSmartReviewContentNotFound() {
        val smartReviewId = "R123"
        val contentId = "R456"

        get("/published-smart-review-content-not-found")
            .param("smartReviewId", smartReviewId)
            .param("contentId", contentId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/published-smart-review-content-not-found"))
            .andExpect(jsonPath("$.message").value("""Smart review content "$contentId" not found for smart review "$smartReviewId"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
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

    @Test
    fun smartReviewAlreadyPublished() {
        val id = "R123"

        get("/smart-review-already-published")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/smart-review-already-published"))
            .andExpect(jsonPath("$.message").value("""Smart review "$id" is already published."""))
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

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/published-smart-review-content-not-found")
        fun publishedSmartReviewContentNotFound(
            @RequestParam smartReviewId: ThingId,
            @RequestParam contentId: ThingId,
        ): Unit = throw PublishedSmartReviewContentNotFound(smartReviewId, contentId)

        @GetMapping("/ontology-entity-not-found")
        fun ontologyEntityNotFound(
            @RequestParam entities: Set<ThingId>,
        ): Unit = throw OntologyEntityNotFound(entities)

        @GetMapping("/invalid-smart-review-text-section-type")
        fun invalidSmartReviewTextSectionType(
            @RequestParam type: ThingId,
        ): Unit = throw InvalidSmartReviewTextSectionType(type)

        @GetMapping("/unrelated-smart-review-section")
        fun unrelatedSmartReviewSection(
            @RequestParam smartReviewId: ThingId,
            @RequestParam smartReviewSectionId: ThingId,
        ): Unit = throw UnrelatedSmartReviewSection(smartReviewId, smartReviewSectionId)

        @GetMapping("/smart-review-section-type-mismatch-must-be-comparison-section")
        fun smartReviewSectionTypeMismatchMustBeComparisonSection(): Unit = throw SmartReviewSectionTypeMismatch.mustBeComparisonSection()

        @GetMapping("/smart-review-section-type-mismatch-must-be-visualization-section")
        fun smartReviewSectionTypeMismatchMustBeVisualizationSection(): Unit = throw SmartReviewSectionTypeMismatch.mustBeVisualizationSection()

        @GetMapping("/smart-review-section-type-mismatch-must-be-resource-section")
        fun smartReviewSectionTypeMismatchMustBeResourceSection(): Unit = throw SmartReviewSectionTypeMismatch.mustBeResourceSection()

        @GetMapping("/smart-review-section-type-mismatch-must-be-predicate-section")
        fun smartReviewSectionTypeMismatchMustBePredicateSection(): Unit = throw SmartReviewSectionTypeMismatch.mustBePredicateSection()

        @GetMapping("/smart-review-section-type-mismatch-must-be-ontology-section")
        fun smartReviewSectionTypeMismatchMustBeOntologySection(): Unit = throw SmartReviewSectionTypeMismatch.mustBeOntologySection()

        @GetMapping("/smart-review-section-type-mismatch-must-be-text-section")
        fun smartReviewSectionTypeMismatchMustBeTextSection(): Unit = throw SmartReviewSectionTypeMismatch.mustBeTextSection()

        @GetMapping("/smart-review-already-published")
        fun smartReviewAlreadyPublished(
            @RequestParam id: ThingId,
        ): Unit = throw SmartReviewAlreadyPublished(id)

        @GetMapping("/smart-review-not-modifiable")
        fun smartReviewNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw SmartReviewNotModifiable(id)
    }
}
