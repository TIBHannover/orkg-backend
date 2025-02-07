package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.adapter.input.rest.exceptions.ObservatoryExceptionUnitTest.TestController
import org.orkg.community.domain.ObservatoryAlreadyExists
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
internal class ObservatoryExceptionUnitTest : MockMvcBaseTest("exceptions") {

    @Test
    fun observatoryAlreadyExistsWithId() {
        val id = ObservatoryId("eeb1ab0f-0ef5-4bee-aba2-2d5cea2f0174")

        get("/observatory-already-exists-with-id")
            .param("id", id.value.toString())
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/observatory-already-exists-with-id"))
            .andExpect(jsonPath("$.message").value("""Observatory with id "$id" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun observatoryAlreadyExistsWithName() {
        val name = "Cool name"

        get("/observatory-already-exists-with-name")
            .param("name", name)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/observatory-already-exists-with-name"))
            .andExpect(jsonPath("$.message").value("""Observatory with name "$name" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun observatoryAlreadyExistsWithDisplayId() {
        val displayId = "cool_name"

        get("/observatory-already-exists-with-display-id")
            .param("displayId", displayId)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.path").value("/observatory-already-exists-with-display-id"))
            .andExpect(jsonPath("$.message").value("""Observatory with display id "$displayId" already exists."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/observatory-already-exists-with-id")
        fun observatoryAlreadyExistsWithId(@RequestParam id: ObservatoryId) {
            throw ObservatoryAlreadyExists.withId(id)
        }

        @GetMapping("/observatory-already-exists-with-name")
        fun observatoryAlreadyExistsWithName(@RequestParam name: String) {
            throw ObservatoryAlreadyExists.withName(name)
        }

        @GetMapping("/observatory-already-exists-with-display-id")
        fun observatoryAlreadyExistsWithDisplayId(@RequestParam displayId: String) {
            throw ObservatoryAlreadyExists.withDisplayId(displayId)
        }
    }
}
