package org.orkg.contenttypes.adapter.input.rest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.domain.ResearchFieldNotAllowed
import org.orkg.contenttypes.domain.ResearchFields
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [ExceptionTest.FakeExceptionController::class, ExceptionHandler::class, FixedClockConfig::class])
internal class ExceptionTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun researchFieldNotAllowed() {
        val id = ResearchFields.science

        get("/researchFieldNotAllowed/{id}", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/researchFieldNotAllowed/$id"))
            .andExpect(jsonPath("$.message").value("""Research field "$id" is not allowed."""))
    }

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/researchFieldNotAllowed/{id}")
        fun researchFieldNotAllowed(@PathVariable id: ThingId) {
            throw ResearchFieldNotAllowed(id)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
