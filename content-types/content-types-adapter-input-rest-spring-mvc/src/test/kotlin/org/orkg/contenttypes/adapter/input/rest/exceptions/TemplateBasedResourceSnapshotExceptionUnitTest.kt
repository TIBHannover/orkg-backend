package org.orkg.contenttypes.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.contenttypes.adapter.input.rest.exceptions.TemplateBasedResourceSnapshotExceptionUnitTest.TestController
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateBasedResourceSnapshotNotFound
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
internal class TemplateBasedResourceSnapshotExceptionUnitTest : MockMvcBaseTest("template-based-resource-snapshots") {
    @Test
    fun templateBasedResourceSnapshotNotFound() {
        val id = "R123"

        get("/template-based-resource-snapshot-not-found")
            .param("id", id)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.error", `is`("Not Found")))
            .andExpect(jsonPath("$.path").value("/template-based-resource-snapshot-not-found"))
            .andExpect(jsonPath("$.message").value("""Template based resource snapshot "$id" not found."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/template-based-resource-snapshot-not-found")
        fun templateBasedResourceSnapshotNotFound(
            @RequestParam id: SnapshotId,
        ): Unit = throw TemplateBasedResourceSnapshotNotFound(id)
    }
}
