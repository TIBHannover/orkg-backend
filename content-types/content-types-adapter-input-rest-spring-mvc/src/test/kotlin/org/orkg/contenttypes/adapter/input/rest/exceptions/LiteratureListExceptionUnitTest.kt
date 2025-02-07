package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.LiteratureListExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.InvalidHeadingSize
import org.orkg.contenttypes.domain.InvalidListSectionEntry
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListSectionTypeMismatch
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.contenttypes.domain.UnrelatedLiteratureListSection
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
internal class LiteratureListExceptionUnitTest : MockMvcBaseTest("literature-lists") {

    @Test
    fun publishedLiteratureListContentNotFound() {
        val literatureListId = "R123"
        val contentId = "R456"

        get("/published-literature-list-content-not-found")
            .param("literatureListId", literatureListId)
            .param("contentId", contentId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/published-literature-list-content-not-found"))
            .andExpect(jsonPath("$.message").value("""Literature list content "$contentId" not found for literature list "$literatureListId"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun literatureListAlreadyPublished() {
        val id = "R123"

        get("/literature-list-already-published")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/literature-list-already-published"))
            .andExpect(jsonPath("$.message").value("""Literature list "$id" is already published."""))
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

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/published-literature-list-content-not-found")
        fun publishedLiteratureListContentNotFound(@RequestParam literatureListId: ThingId, @RequestParam contentId: ThingId) {
            throw PublishedLiteratureListContentNotFound(literatureListId, contentId)
        }

        @GetMapping("/literature-list-already-published")
        fun literatureListAlreadyPublished(@RequestParam id: ThingId) {
            throw LiteratureListAlreadyPublished(id)
        }

        @GetMapping("/errors/literature-list-not-found")
        fun literatureListNotFound(@RequestParam id: ThingId) {
            throw LiteratureListNotFound(id)
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

        @GetMapping("/literature-list-not-modifiable")
        fun literatureListNotModifiable(@RequestParam id: ThingId) {
            throw LiteratureListNotModifiable(id)
        }
    }
}
