package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.time.Clock
import java.util.*
import org.junit.jupiter.api.Test
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.RetrieveThingUseCase
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectResource
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ThingController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [ThingController::class])
internal class ThingControllerUnitTest : MockMvcBaseTest("things") {

    @MockkBean
    private lateinit var thingService: RetrieveThingUseCase

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @Autowired
    private lateinit var clock: Clock

    @Test
    fun getSingle() {
        val thing = createResource()

        every { thingService.findById(any()) } returns Optional.of(thing)
        every { statementService.countIncomingStatements(thing.id) } returns 23

        documentedGetRequestTo("/api/things/{id}", thing.id)
            .perform()
            .andExpect(status().isOk)
            .andExpectResource()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the thing to retrieve.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { thingService.findById(any()) }
        verify(exactly = 1) { statementService.countIncomingStatements(thing.id) }
    }
}
