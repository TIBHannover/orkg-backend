package org.orkg.contenttypes.adapter.input.rest

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ComparisonControllerV2::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [ComparisonControllerV2::class])
internal class ComparisonControllerV2UnitTest : MockMvcBaseTest("comparisons") {
    @Test
    @DisplayName("Given a comparison, when it is fetched by id, then status is 406 NOT ACCEPTABLE")
    fun findById() {
        get("/api/comparisons/{id}", ThingId("R123"))
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @DisplayName("Given several comparisons, when fetched, then status is 406 NOT ACCEPTABLE")
    fun findAll() {
        get("/api/comparisons")
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a create comparison request, then status is 406 NOT ACCEPTABLE")
    fun create() {
        post("/api/comparisons")
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotAcceptable)
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a update comparison request, then status is 406 NOT ACCEPTABLE")
    fun update() {
        put("/api/comparisons/{id}", ThingId("R123"))
            .accept(COMPARISON_JSON_V2)
            .contentType(COMPARISON_JSON_V2)
            .perform()
            .andExpect(status().isNotAcceptable)
    }
}
