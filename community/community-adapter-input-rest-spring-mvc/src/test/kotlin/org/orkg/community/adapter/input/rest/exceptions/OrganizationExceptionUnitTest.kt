package org.orkg.community.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.OrganizationId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.community.adapter.input.rest.exceptions.OrganizationExceptionUnitTest.TestController
import org.orkg.community.domain.OrganizationNotFound
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
internal class OrganizationExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun organizationNotFound() {
        val id = OrganizationId("f9965b2a-5222-45e1-8ef8-dbd8ce1f57bc")

        get("/organization-not-found")
            .param("id", id.value.toString())
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/organization-not-found"))
            .andExpect(jsonPath("$.message").value("""Organization "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/organization-not-found")
        fun organizationNotFound(
            @RequestParam id: OrganizationId,
        ): Unit = throw OrganizationNotFound(id)
    }
}
