package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.LiteratureListControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.LiteratureListAlreadyPublished
import org.orkg.contenttypes.domain.PublishedLiteratureListContentNotFound
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestComponent
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@WebMvcTest
@ContextConfiguration(classes = [FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class LiteratureListControllerExceptionUnitTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

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

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
