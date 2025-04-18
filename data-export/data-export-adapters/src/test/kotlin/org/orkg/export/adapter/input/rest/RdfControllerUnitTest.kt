package org.orkg.export.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.export.adapter.input.rest.RdfController.Companion.DUMP_ENDPOINT
import org.orkg.export.adapter.input.rest.RdfController.Companion.HINTS_ENDPOINT
import org.orkg.export.input.ExportRDFUseCase
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.annotations.TestWithMockAdmin
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.core.task.TaskExecutor
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [RdfController::class, ExceptionHandler::class, FixedClockConfig::class, WebMvcConfiguration::class])
@WebMvcTest(controllers = [RdfController::class])
internal class RdfControllerUnitTest : MockMvcBaseTest("rdf-hints") {
    @MockkBean
    private lateinit var resourceRepository: ResourceRepository

    @MockkBean
    private lateinit var predicateRepository: PredicateRepository

    @MockkBean
    private lateinit var classRepository: ClassRepository

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var rdfService: ExportRDFUseCase

    @MockkBean
    private lateinit var taskExecutor: TaskExecutor

    @Test
    fun legacyRedirectToDump() {
        documentedGetRequestTo(DUMP_ENDPOINT)
            .accept("application/n-triples")
            .perform()
            .andExpect(status().isMovedPermanently)
            .andExpect(header().string("Location", endsWith("/files/rdf-dumps/rdf-export-orkg.nt")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("Location to the rdf dump.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())
    }

    @Test
    fun testFilterResources() {
        val someId = ThingId("R1234")
        every {
            resourceRepository.findAll(
                label = any<FuzzySearchString>(),
                pageable = any<Pageable>()
            )
        } returns pageOf(
            createResource(id = someId, label = "Resource 1234")
        )

        every { statementService.countAllIncomingStatementsById(setOf(someId)) } returns mapOf(someId to 5L)

        // TODO: tests else-branch, not ideal
        documentedGetRequestTo(HINTS_ENDPOINT)
            .param("q", "1")
            .param("exact", "false")
            .param("type", "item")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Resource 1234")))
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("q").description("The search string for the label."),
                        parameterWithName("exact").description("Determine if exact search should be performed. (Default: `false`)"),
                        parameterWithName("type").description("The type of entity to be retrieved. Can be one of `property`, `class`, or `item`."),
                    )
                )
            )

        verify(exactly = 1) { statementService.countAllIncomingStatementsById(setOf(someId)) }
        verify(exactly = 1) {
            resourceRepository.findAll(
                label = withArg { searchString -> searchString.shouldBeInstanceOf<FuzzySearchString> { it.input shouldBe "1" } },
                pageable = any<Pageable>()
            )
        }
    }

    @Test
    fun testFilterClasses() {
        every {
            classRepository.findAll(
                label = any<FuzzySearchString>(),
                pageable = any<Pageable>()
            )
        } returns PageImpl(
            listOf(createClass(id = ThingId("C1234"), label = "Class 1234"))
        )
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        get(HINTS_ENDPOINT)
            .param("q", "1234")
            .param("type", "class")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Class 1234")))

        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
        verify(exactly = 1) {
            classRepository.findAll(
                label = withArg { searchString -> searchString.shouldBeInstanceOf<FuzzySearchString> { it.input shouldBe "1234" } },
                pageable = any<Pageable>()
            )
        }
    }

    @Test
    fun testFilterPredicates() {
        every {
            predicateRepository.findAll(
                label = any<FuzzySearchString>(),
                pageable = any<Pageable>()
            )
        } returns pageOf(
            listOf(createPredicate(id = ThingId("P1234"), label = "Predicate 1234"))
        )
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        get(HINTS_ENDPOINT)
            .param("q", "1234")
            .param("type", "property")
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Predicate 1234")))

        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
        verify(exactly = 1) {
            predicateRepository.findAll(
                label = withArg { searchString -> searchString.shouldBeInstanceOf<FuzzySearchString> { it.input shouldBe "1234" } },
                pageable = any<Pageable>()
            )
        }
    }

    @Test
    @TestWithMockAdmin
    fun createRdfDump() {
        every { taskExecutor.execute(any()) } just runs

        documentedPostRequestTo("/api/admin/rdf/dump")
            .perform()
            .andExpect(status().isNoContent)
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { taskExecutor.execute(any()) }
    }
}
