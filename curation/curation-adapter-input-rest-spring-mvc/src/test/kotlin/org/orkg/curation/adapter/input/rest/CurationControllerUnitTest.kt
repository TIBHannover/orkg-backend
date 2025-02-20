package org.orkg.curation.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.curation.input.CurationUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.testing.andExpectClass
import org.orkg.testing.andExpectPredicate
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [CurationController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [CurationController::class])
internal class CurationControllerUnitTest : MockMvcBaseTest("curation") {
    @MockkBean
    private lateinit var service: CurationUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Test
    @DisplayName("Given several predicates, when fetching all predicates without descriptions, then status is 200 OK and predicates are returned")
    fun findAllPredicatesWithoutDescriptions() {
        every { service.findAllPredicatesWithoutDescriptions(any()) } returns pageOf(createPredicate())

        documentedGetRequestTo("/api/curation/predicates-without-descriptions")
            .perform()
            .andExpect(status().isOk)
            .andExpectPredicate("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllPredicatesWithoutDescriptions(any()) }
    }

    @Test
    @DisplayName("Given several classes, when fetching all classes without descriptions, then status is 200 OK and classes are returned")
    fun findAllClassesWithoutDescriptions() {
        every { service.findAllClassesWithoutDescriptions(any()) } returns pageOf(createClass())

        documentedGetRequestTo("/api/curation/classes-without-descriptions")
            .perform()
            .andExpect(status().isOk)
            .andExpectClass("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllClassesWithoutDescriptions(any()) }
    }
}
