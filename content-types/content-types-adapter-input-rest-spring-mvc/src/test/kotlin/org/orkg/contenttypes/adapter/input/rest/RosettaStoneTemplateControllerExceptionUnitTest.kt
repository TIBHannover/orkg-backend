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
import org.orkg.contenttypes.domain.RosettaStoneTemplateInUse
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotModifiable
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
    fun rosettaStoneTemplateInUse() {
        val id = "R123"

        get("/rosetta-stone-template-in-use")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-template-in-use"))
            .andExpect(jsonPath("$.message").value("""Unable to delete rosetta stone template "$id" because it is used in at least one (rosetta stone) statement."""))
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

        @GetMapping("/rosetta-stone-template-in-use")
        fun rosettaStoneTemplateInUse(@RequestParam id: ThingId) {
            throw RosettaStoneTemplateInUse(id)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
