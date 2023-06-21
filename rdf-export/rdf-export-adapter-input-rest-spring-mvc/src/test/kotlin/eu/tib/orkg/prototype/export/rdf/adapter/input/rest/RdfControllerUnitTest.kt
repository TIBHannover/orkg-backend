package eu.tib.orkg.prototype.export.rdf.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import eu.tib.orkg.prototype.core.rest.ExceptionHandler
import eu.tib.orkg.prototype.export.rdf.adapter.input.rest.RdfController.Companion.DUMP_ENDPOINT
import eu.tib.orkg.prototype.export.rdf.adapter.input.rest.RdfController.Companion.HINTS_ENDPOINT
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.domain.model.FuzzySearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.StatementService
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.testing.annotations.UsesMocking
import eu.tib.orkg.prototype.testing.spring.restdocs.RestDocsTest
import eu.tib.orkg.prototype.testing.spring.restdocs.documentedGetRequestTo
import io.mockk.every
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createPredicate
import org.orkg.statements.testing.createResource
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [RdfController::class, ExceptionHandler::class])
@WebMvcTest(controllers = [RdfController::class])
@UsesMocking
internal class RdfControllerUnitTest : RestDocsTest("rdf-hints") {

    @MockkBean
    private lateinit var resourceRepository: ResourceRepository

    @MockkBean
    private lateinit var predicateRepository: PredicateRepository

    @MockkBean
    private lateinit var classRepository: ClassRepository

    @MockkBean
    private lateinit var statementService: StatementService

    @MockkBean
    private lateinit var templateRepository: TemplateRepository

    @MockkBean
    private lateinit var featureFlagService: FeatureFlagService

    @Test
    fun legacyRedirectToDump() {
        mockMvc.perform(get(DUMP_ENDPOINT))
            .andExpect(status().isMovedPermanently)
            .andExpect(header().string("Location", endsWith("/files/rdf-dumps/rdf-export-orkg.nt")))
    }

    @Test
    fun testFilterResources() {
        val someId = ThingId("R1234")
        // TODO: Search strings are not comparable, so they cannot be used here.
        every { resourceRepository.findAllByLabel(any<FuzzySearchString>(), any<Pageable>()) } returns PageImpl(
            listOf(createResource(id = someId, label = "Resource 1234"))
        )
        every { statementService.countStatementsAboutResources(setOf(someId)) } returns mapOf(someId to 5L)
        every { featureFlagService.isFormattedLabelsEnabled() } returns false

        // TODO: tests else-branch, not ideal
        mockMvc.perform(
            documentedGetRequestTo(HINTS_ENDPOINT)
                .param("q", "1")
                .param("exact", "false")
                .param("type", "item")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Resource 1234")))
            .andDo(
                documentationHandler.document(
                    requestParameters(
                        parameterWithName("q").description("The search string for the label."),
                        parameterWithName("exact").description("Determine if exact search should be performed. (Default: `false`)"),
                        parameterWithName("type").description("The type of entity to be retrieved. Can be one of `property`, `class`, or `item`."),
                    )
                )
            )
    }

    @Test
    fun testFilterClasses() {
        // TODO: Search strings are not comparable, so they cannot be used here.
        every { classRepository.findAllByLabel(any<FuzzySearchString>(), any<Pageable>()) } returns PageImpl(
            listOf(createClass(id = ThingId("C1234"), label = "Class 1234"))
        )

        mockMvc.perform(get(HINTS_ENDPOINT).param("q", "1234").param("type", "class"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Class 1234")))
    }

    @Test
    fun testFilterPredicates() {
        // TODO: Search strings are not comparable, so they cannot be used here.
        every { predicateRepository.findAllByLabel(any<FuzzySearchString>(), any<Pageable>()) } returns PageImpl(
            listOf(createPredicate(id = ThingId("P1234"), label = "Predicate 1234"))
        )

        mockMvc.perform(get(HINTS_ENDPOINT).param("q", "1234").param("type", "property"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].label", `is`("Predicate 1234")))
    }
}
