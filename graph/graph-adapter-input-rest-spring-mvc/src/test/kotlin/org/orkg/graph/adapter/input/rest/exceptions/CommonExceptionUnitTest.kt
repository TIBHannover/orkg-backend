package org.orkg.graph.adapter.input.rest.exceptions

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.graph.adapter.input.rest.exceptions.CommonExceptionUnitTest.TestController
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.testing.MockUserId
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
internal class CommonExceptionUnitTest : MockMvcBaseTest("exceptions") {
    @Test
    fun invalidLabel() {
        get("/invalid-label")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("label"))
            .andExpect(jsonPath("$.errors[0].message").value("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-label"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidLabelWithProperty() {
        get("/invalid-label")
            .param("property", "title")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("title"))
            .andExpect(jsonPath("$.errors[0].message").value("""A label must not be blank or contain newlines and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-label"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDescription() {
        get("/invalid-description")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("description"))
            .andExpect(jsonPath("$.errors[0].message").value("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-description"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun invalidDescriptionWithProperty() {
        get("/invalid-description")
            .param("property", "contents")
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.error", `is`("Bad Request")))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("contents"))
            .andExpect(jsonPath("$.errors[0].message").value("""A description must not be blank and must be at most $MAX_LABEL_LENGTH characters long."""))
            .andExpect(jsonPath("$.path").value("/invalid-description"))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun neitherOwnerNorCuratorDelete() {
        val contributorId = ContributorId(MockUserId.USER)

        get("/neither-owner-nor-creator-delete")
            .param("contributorId", contributorId.toString())
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/neither-owner-nor-creator-delete"))
            .andExpect(jsonPath("$.message").value("""Contributor <$contributorId> does not own the entity to be deleted and is not a curator."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun neitherOwnerNorCuratorVisibility() {
        val id = "R123"

        get("/neither-owner-nor-creator-visibility")
            .param("id", id)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/neither-owner-nor-creator-visibility"))
            .andExpect(jsonPath("$.message").value("""Insufficient permissions to change visibility of entity "$id"."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun notACurator() {
        val contributorId = "62bfeea3-4210-436d-b953-effa5a07ed64"

        get("/not-a-curator")
            .param("contributorId", contributorId)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/not-a-curator"))
            .andExpect(jsonPath("$.message").value("""Contributor <$contributorId> is not a curator."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @Test
    fun notACuratorCannotChangeVerifiedStatus() {
        val contributorId = "62bfeea3-4210-436d-b953-effa5a07ed64"

        get("/not-a-curator-cannot-change-verified-status")
            .param("contributorId", contributorId)
            .perform()
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
            .andExpect(jsonPath("$.error", `is`("Forbidden")))
            .andExpect(jsonPath("$.path").value("/not-a-curator-cannot-change-verified-status"))
            .andExpect(jsonPath("$.message").value("""Cannot change verified status: Contributor <$contributorId> is not a curator."""))
            .andExpect(jsonPath("$.timestamp", `is`(notNullValue())))
    }

    @TestComponent
    @RestController
    internal class TestController {
        @GetMapping("/invalid-label")
        fun invalidLabel(): Unit = throw InvalidLabel()

        @GetMapping("/invalid-label", params = ["property"])
        fun invalidLabel(
            @RequestParam property: String,
        ): Unit = throw InvalidLabel(property)

        @GetMapping("/invalid-description")
        fun invalidInvalidDescription(): Unit = throw InvalidDescription()

        @GetMapping("/invalid-description", params = ["property"])
        fun invalidInvalidDescription(
            @RequestParam property: String,
        ): Unit = throw InvalidDescription(property)

        @GetMapping("/neither-owner-nor-creator-delete")
        fun neitherOwnerNorCuratorDelete(
            @RequestParam contributorId: ContributorId,
        ): Unit = throw NeitherOwnerNorCurator(contributorId)

        @GetMapping("/neither-owner-nor-creator-visibility")
        fun neitherOwnerNorCuratorVisibility(
            @RequestParam id: ThingId,
        ): Unit = throw NeitherOwnerNorCurator.cannotChangeVisibility(id)

        @GetMapping("/not-a-curator")
        fun notACurator(
            @RequestParam contributorId: ContributorId,
        ): Unit = throw NotACurator(contributorId)

        @GetMapping("/not-a-curator-cannot-change-verified-status")
        fun notACuratorCannotChangeVerifiedStatus(
            @RequestParam contributorId: ContributorId,
        ): Unit = throw NotACurator.cannotChangeVerifiedStatus(contributorId)
    }
}
