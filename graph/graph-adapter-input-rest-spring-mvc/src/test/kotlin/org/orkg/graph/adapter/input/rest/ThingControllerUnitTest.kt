package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import java.time.Clock
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.RetrieveThingUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectResource
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ThingController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [ThingController::class])
internal class ThingControllerUnitTest : RestDocsTest("things") {

    @MockkBean
    private lateinit var thingService: RetrieveThingUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Autowired
    private lateinit var clock: Clock

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingService, statementService, formattedLabelService, flags)
    }

    @Test
    fun getSingle() {
        val thing = createResource()

        every { thingService.findById(any()) } returns Optional.of(thing)
        every { statementService.countIncomingStatements(thing.id) } returns 23

        documentedGetRequestTo("/api/things/{id}", thing.id)
            .accept(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { thingService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatements(thing.id) }
    }
}
