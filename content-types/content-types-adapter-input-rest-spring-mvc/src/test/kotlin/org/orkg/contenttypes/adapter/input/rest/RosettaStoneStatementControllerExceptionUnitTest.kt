package org.orkg.contenttypes.adapter.input.rest

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.RosettaStoneStatementControllerExceptionUnitTest.FakeExceptionController
import org.orkg.contenttypes.domain.CannotDeleteIndividualRosettaStoneStatementVersion
import org.orkg.contenttypes.domain.MissingInputPositions
import org.orkg.contenttypes.domain.NestedRosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementInUse
import org.orkg.contenttypes.domain.RosettaStoneStatementNotModifiable
import org.orkg.contenttypes.domain.TooManyInputPositions
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
internal class RosettaStoneStatementControllerExceptionUnitTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @Test
    fun tooManyInputPositions() {
        val exceptedCount = "5"
        val templateId = "R123"

        get("/too-many-input-positions")
            .param("exceptedCount", exceptedCount)
            .param("templateId", templateId)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/too-many-input-positions"))
            .andExpect(jsonPath("$.message").value("""Too many input positions for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun missingInputPositions() {
        val exceptedCount = "5"
        val templateId = "R123"
        val missingCount = "2"

        get("/missing-input-positions")
            .param("exceptedCount", exceptedCount)
            .param("templateId", templateId)
            .param("missingCount", missingCount)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/missing-input-positions"))
            .andExpect(jsonPath("$.message").value("""Missing input for $missingCount input positions for rosetta stone statement of template "$templateId". Expected exactly $exceptedCount input positions."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneStatementNotModifiable() {
        val id = "R123"

        get("/rosetta-stone-statement-not-modifiable")
            .param("id", id)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-statement-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Rosetta stone statement "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun cannotDeleteIndividualRosettaStoneStatementVersion() {
        get("/cannot-delete-individual-rosetta-stone-statement-version")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/cannot-delete-individual-rosetta-stone-statement-version"))
            .andExpect(jsonPath("$.message").value("""Cannot delete individual versions of rosetta stone statements."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun rosettaStoneStatementInUse() {
        val id = "R123"

        get("/rosetta-stone-statement-in-use")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/rosetta-stone-statement-in-use"))
            .andExpect(jsonPath("$.message").value("""Unable to delete rosetta stone statement "$id" because it is used in at least one (rosetta stone) statement."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun nestedRosettaStoneStatement() {
        val id = "R123"
        val index = "4"

        get("/nested-rosetta-stone-statement")
            .param("id", id)
            .param("index", index)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/nested-rosetta-stone-statement"))
            .andExpect(jsonPath("$.message").value("""Rosetta stone statement "$id" for input position $index already contains a rosetta stone statement in one of its input positions."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class FakeExceptionController {
        @GetMapping("/too-many-input-positions")
        fun tooManyInputPositions(@RequestParam exceptedCount: Int, @RequestParam templateId: ThingId) {
            throw TooManyInputPositions(exceptedCount, templateId)
        }

        @GetMapping("/missing-input-positions")
        fun tooManyInputPositions(
            @RequestParam exceptedCount: Int,
            @RequestParam templateId: ThingId,
            @RequestParam missingCount: Int
        ) {
            throw MissingInputPositions(exceptedCount, templateId, missingCount)
        }

        @GetMapping("/rosetta-stone-statement-not-modifiable")
        fun rosettaStoneStatementNotModifiable(@RequestParam id: ThingId) {
            throw RosettaStoneStatementNotModifiable(id)
        }

        @GetMapping("/cannot-delete-individual-rosetta-stone-statement-version")
        fun cannotDeleteIndividualRosettaStoneStatementVersion() {
            throw CannotDeleteIndividualRosettaStoneStatementVersion()
        }

        @GetMapping("/rosetta-stone-statement-in-use")
        fun rosettaStoneStatementInUse(@RequestParam id: ThingId) {
            throw RosettaStoneStatementInUse(id)
        }

        @GetMapping("/nested-rosetta-stone-statement")
        fun nestedRosettaStoneStatement(@RequestParam id: ThingId, @RequestParam index: Int) {
            throw NestedRosettaStoneStatement(id, index)
        }
    }

    private fun MockHttpServletRequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}
