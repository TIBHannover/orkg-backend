package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.configuration.WebMvcConfiguration
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.community.input.RetrieveContributorUseCase
import org.orkg.contenttypes.domain.ResearchFieldHierarchyEntry
import org.orkg.contenttypes.domain.ResearchFieldWithChildCount
import org.orkg.contenttypes.input.ResearchFieldHierarchyUseCases
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ResearchFieldNotFound
import org.orkg.graph.input.FormattedLabelUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectResearchFieldHierarchyEntry
import org.orkg.testing.andExpectResource
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.Page
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(
    classes = [
        ResearchFieldHierarchyController::class,
        ExceptionHandler::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        WebMvcConfiguration::class
    ]
)
@WebMvcTest(controllers = [ResearchFieldHierarchyController::class])
internal class ResearchFieldHierarchyControllerUnitTest : MockMvcBaseTest("research-fields") {
    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var formattedLabelService: FormattedLabelUseCases

    @MockkBean
    private lateinit var userRepository: RetrieveContributorUseCase

    @MockkBean
    private lateinit var service: ResearchFieldHierarchyUseCases

    @Test
    @DisplayName("Given a parent research field id, when searched for its children, then status is 200 OK and children research field ids are returned")
    fun findChildren() {
        val parentId = ThingId("R123")
        val subfieldId = ThingId("subfield")
        val response = ResearchFieldWithChildCount(createResearchField(subfieldId), 1)

        every { service.findAllChildrenByAncestorId(parentId, any()) } returns pageOf(response)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields/{id}/children", parentId)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].resource.id").value(response.resource.id.value))
            .andExpect(jsonPath("$.content[0].child_count").value(response.childCount))
            .andExpect(jsonPath("$.page.total_elements").value(1))
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

        verify(exactly = 1) { service.findAllChildrenByAncestorId(parentId, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a parent research field id, when service reports the parent research field cannot be found while searching for its children, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val exception = ResearchFieldNotFound(parentId)

        every { service.findAllChildrenByAncestorId(parentId, any()) } throws exception

        get("/api/research-fields/{id}/children", parentId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$parentId/children"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findAllChildrenByAncestorId(parentId, any()) }
    }

    @Test
    @DisplayName("Given a subfield id, when searched for its parent, then status is 200 OK and parent research field is returned")
    fun findParents() {
        val parentId = ThingId("parent")
        val subfieldId = ThingId("R123")

        every { service.findAllParentsByChildId(subfieldId, any()) } returns pageOf(createResearchField(parentId))
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields/{id}/parents", subfieldId)
            .perform()
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

        verify(exactly = 1) { service.findAllParentsByChildId(subfieldId, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a subfield id, when service reports the subfield cannot be found while searching for its parent, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findAllParentsByChildId(subfieldId, any()) } throws exception

        get("/api/research-fields/{id}/parents", subfieldId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/parents"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findAllParentsByChildId(subfieldId, any()) }
    }

    @Test
    fun `Given a subfield id, when the parent research field cannot be found, then status is 200 OK and empty page is returned`() {
        val subfieldId = ThingId("subfield")

        every { service.findAllParentsByChildId(subfieldId, any()) } returns Page.empty()

        get("/api/research-fields/{id}/parents", subfieldId)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("$.content", empty<Collection<*>>()))

        verify(exactly = 1) { service.findAllParentsByChildId(subfieldId, any()) }
    }

    @Test
    @DisplayName("Given a subfield id, when searched for its root, then status is 200 OK and root research field is returned")
    fun findRoots() {
        val rootId = ThingId("root")
        val subfieldId = ThingId("subfield")
        val root = createResearchField(rootId)

        every { service.findAllRootsByDescendantId(subfieldId, any()) } returns pageOf(root)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields/{id}/roots", subfieldId)
            .perform()
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

        verify(exactly = 1) { service.findAllRootsByDescendantId(subfieldId, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a subfield id, when service reports the subfield cannot be found while searching for its root, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findAllRootsByDescendantId(subfieldId, any()) } throws exception

        get("/api/research-fields/{id}/roots", subfieldId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/roots"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findAllRootsByDescendantId(subfieldId, any()) }
    }

    @Test
    fun `Given a subfield id, when searched for its root but it has no parent research field, then status is 200 OK and empty page is returned`() {
        val subfieldId = ThingId("subfield")

        every { service.findAllRootsByDescendantId(subfieldId, any()) } returns Page.empty()

        get("/api/research-fields/{id}/roots", subfieldId)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpect(jsonPath("$.content", empty<Collection<*>>()))

        verify(exactly = 1) { service.findAllRootsByDescendantId(subfieldId, any()) }
    }

    @Test
    @DisplayName("Given a research field id, when the research field hierarchy is fetched, then status is 200 OK")
    fun findHierarchy() {
        val subfieldId = ThingId("subfield")
        val parentId = ThingId("parent")
        val childResearchField = createResearchField().copy(id = subfieldId)
        val entry = ResearchFieldHierarchyEntry(childResearchField, setOf(parentId))

        every { service.findResearchFieldHierarchyByResearchFieldId(subfieldId, any()) } returns pageOf(entry)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields/{id}/hierarchy", subfieldId)
            .perform()
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

        verify(exactly = 1) { service.findResearchFieldHierarchyByResearchFieldId(subfieldId, any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    @Test
    fun `Given a research field id, when service reports missing research field while fetched the research field hierarchy, then status is 404 NOT FOUND`() {
        val subfieldId = ThingId("subfield")
        val exception = ResearchFieldNotFound(subfieldId)

        every { service.findResearchFieldHierarchyByResearchFieldId(subfieldId, any()) } throws exception

        get("/api/research-fields/{id}/hierarchy", subfieldId)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/research-fields/$subfieldId/hierarchy"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { service.findResearchFieldHierarchyByResearchFieldId(subfieldId, any()) }
    }

    @Test
    @DisplayName("When searching for all root research fields, then status is 200 OK and root research fields is returned")
    fun findAllRoots() {
        val rootId = ThingId("root")
        val root = createResearchField(rootId)

        every { service.findAllRoots(any()) } returns pageOf(root)
        every { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) } returns emptyMap()

        documentedGetRequestTo("/api/research-fields/roots")
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectResource("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { service.findAllRoots(any()) }
        verify(exactly = 1) { statementService.countAllIncomingStatementsById(any<Set<ThingId>>()) }
    }

    private fun createResearchField(id: ThingId = ThingId("R1")) =
        createResource(id = id, classes = setOf(Classes.researchField))
}
