package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.exceptions.ResourceExceptionUnitTest.TestController
import org.orkg.graph.domain.ExternalResourceNotFound
import org.orkg.graph.domain.ResourceNotModifiable
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
internal class ResourceExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun resourceNotModifiable() {
        val id = ThingId("R123")

        get("/resource-not-modifiable")
            .param("id", id.value)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/resource-not-modifiable"))
            .andExpect(jsonPath("$.message").value("""Resource "$id" is not modifiable."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun externalResourceNotFound() {
        val id = "R123"
        val ontologyId = "skos"

        get("/external-resource-not-found")
            .param("id", id)
            .param("ontologyId", ontologyId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/external-resource-not-found"))
            .andExpect(jsonPath("$.message").value("""External resource "$id" for ontology "$ontologyId" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/resource-not-modifiable")
        fun resourceNotModifiable(
            @RequestParam id: ThingId,
        ): Unit = throw ResourceNotModifiable(id)

        @GetMapping("/external-resource-not-found")
        fun externalResourceNotFound(
            @RequestParam ontologyId: String,
            @RequestParam id: String,
        ): Unit = throw ExternalResourceNotFound(ontologyId, id)
    }
}
