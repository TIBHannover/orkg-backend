package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.contenttypes.input.ResearchFieldHierarchyUseCases
import org.orkg.featureflags.output.FeatureFlagService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResearchFieldHierarchyEntry
import org.orkg.testing.andExpectResource
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [ResearchFieldHierarchyController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ResearchFieldHierarchyController::class])
@DisplayName("Given a ResearchField controller")
internal class ResearchFieldHierarchyControllerUnitTest : RestDocsTest("research-fields") {

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var formattedLabelRepository: FormattedLabelRepository

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var flags: FeatureFlagService

    @Suppress("unused") // Required to properly initialize ApplicationContext, but not used in the test.
    @MockkBean
    private lateinit var userRepository: RetrieveContributorUseCase

    @MockkBean
    private lateinit var service: ResearchFieldHierarchyUseCases

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(statementService, formattedLabelRepository, flags, service)
    }

    @Test
    @DisplayName("Given a parent research field id, when searched for its children, then status is 200 OK and children research field ids are returned")
    fun findChildren() {
        val parentId = ThingId("R123")
        val subfieldId = ThingId("subfield")
        val response = ResearchFieldWithChildCount(createResearchField(subfieldId), 1)

        every { service.findChildren(parentId, any()) } returns pageOf(response)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(documentedGetRequestTo("/api/research-fields/{id}/children", parentId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].resource.id").value(response.resource.id.value))
            .andExpect(jsonPath("$.content[0].child_count").value(response.childCount))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpectPage()
            .andExpectResource("$.content[*].resource")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The research field id to fetch the direct subfields of.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findChildren(parentId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given a parent research field id, when service reports the parent research field cannot be found while searching for its children, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val exception = ResearchFieldNotFound(parentId)

        every { service.findChildren(parentId, any()) } throws exception

        get("/api/research-fields/$parentId/children")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$parentId/children"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findChildren(parentId, any()) }
    }

    @Test
    @DisplayName("Given a subfield id, when searched for its parent, then status is 200 OK and parent research field is returned")
    fun findParents() {
        val parentId = ThingId("parent")
        val subfieldId = ThingId("R123")

        every { service.findParents(subfieldId, any()) } returns pageOf(createResearchField(parentId))
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(documentedGetRequestTo("/api/research-fields/{id}/parents", subfieldId))
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The research field id to fetch the direct parent research fields of.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findParents(subfieldId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given a subfield id, when service reports the subfield cannot be found while searching for its parent, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findParents(subfieldId, any()) } throws exception

        get("/api/research-fields/$subfieldId/parents")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/parents"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findParents(subfieldId, any()) }
    }

    @Test
    fun `Given a subfield id, when the parent research field cannot be found, then status is 200 OK and empty page is returned`() {
        val subfieldId = ThingId("subfield")

        every { service.findParents(subfieldId, any()) } returns Page.empty()
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        get("/api/research-fields/$subfieldId/parents")
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("$.content", empty<Collection<*>>()))

        verify(exactly = 1) { service.findParents(subfieldId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    @DisplayName("Given a subfield id, when searched for its root, then status is 200 OK and root research field is returned")
    fun findRoots() {
        val rootId = ThingId("root")
        val subfieldId = ThingId("subfield")
        val root = createResearchField(rootId)

        every { service.findRoots(subfieldId, any()) } returns pageOf(root)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(documentedGetRequestTo("/api/research-fields/{id}/roots", subfieldId))
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The research field id to fetch the roots research fields of.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findRoots(subfieldId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given a subfield id, when service reports the subfield cannot be found while searching for its root, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findRoots(subfieldId, any()) } throws exception

        get("/api/research-fields/$subfieldId/roots")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/roots"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findRoots(subfieldId, any()) }
    }

    @Test
    fun `Given a subfield id, when searched for its root but it has no parent research field, then status is 200 OK and empty page is returned`() {
        val subfieldId = ThingId("subfield")

        every { service.findRoots(subfieldId, any()) } returns Page.empty()
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        get("/api/research-fields/$subfieldId/roots")
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("$.content", empty<Collection<*>>()))

        verify(exactly = 1) { service.findRoots(subfieldId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    @DisplayName("Given a research field id, when the research field hierarchy is fetched, then status is 200 OK")
    fun findHierarchy() {
        val subfieldId = ThingId("subfield")
        val parentId = ThingId("parent")
        val childResearchField = createResearchField().copy(id = subfieldId)
        val entry = ResearchFieldHierarchyEntry(childResearchField, setOf(parentId))

        every { service.findResearchFieldHierarchy(subfieldId, any()) } returns pageOf(entry)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(documentedGetRequestTo("/api/research-fields/{id}/hierarchy", subfieldId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].resource.id").value(childResearchField.id.value))
            .andExpect(jsonPath("$.content[0].parent_ids[0]").value(parentId.value))
            .andExpectPage()
            .andExpectResearchFieldHierarchyEntry("$.content[*]")
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The research field id to fetch the hierarchy of.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findResearchFieldHierarchy(subfieldId, any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    @Test
    fun `Given a research field id, when service reports missing research field while fetched the research field hierarchy, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findResearchFieldHierarchy(subfieldId, any()) } throws exception

        get("/api/research-fields/$subfieldId/hierarchy")
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/hierarchy"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findResearchFieldHierarchy(subfieldId, any()) }
    }

    @Test
    @DisplayName("When searching for all root research fields, then status is 200 OK and root research fields is returned")
    fun findAllRoots() {
        val rootId = ThingId("root")
        val root = createResearchField(rootId)

        every { service.findAllRoots(any()) } returns pageOf(root)
        every { statementService.countStatementsAboutResources(any()) } returns emptyMap()
        every { flags.isFormattedLabelsEnabled() } returns false

        mockMvc.perform(documentedGetRequestTo("/api/research-fields/roots"))
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllRoots(any()) }
        verify(exactly = 1) { statementService.countStatementsAboutResources(any()) }
        verify(exactly = 1) { flags.isFormattedLabelsEnabled() }
    }

    private fun get(uri: String) = mockMvc.perform(MockMvcRequestBuilders.get(uri))

    private fun createResearchField(id: ThingId = ThingId("R1")) =
        createResource(id = id, classes = setOf(Classes.researchField))
}
