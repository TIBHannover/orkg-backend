package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.LiteratureListControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.restdocs.MockMvcBaseTest
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
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class LiteratureListControllerExceptionUnitTest : MockMvcBaseTest("literature-lists") {

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

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/published-literature-list-content-not-found")
        fun publishedLiteratureListContentNotFound(@RequestParam literatureListId: ThingId, @RequestParam contentId: ThingId) {
            throw PublishedLiteratureListContentNotFound(literatureListId, contentId)
        }

        @GetMapping("/literature-list-already-published")
        fun literatureListAlreadyPublished(@RequestParam id: ThingId) {
            throw LiteratureListAlreadyPublished(id)
        }
    }
}
