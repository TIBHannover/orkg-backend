package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@Transactional
internal class VisualizationControllerIntegrationTest : MockMvcBaseTest("visualizations") {

    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)

        listOf(
            Predicates.hasAuthors,
            Predicates.hasEvaluation,
            Predicates.hasORCID,
            Predicates.reference,
            Predicates.hasWebsite,
            Predicates.description,
            Predicates.hasListElement
        ).forEach { predicateService.createPredicate(it) }

        classService.createClasses("Visualization", "Contribution", "Problem", "ResearchField", "Author", "Venue", "Result")

        // Example specific entities

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        resourceService.createResource(id = "R123", label = "Author with id", classes = setOf("Author"))

        statementService.create(
            subject = resourceService.createResource(
                id = "R456",
                label = "Author with id and orcid",
                classes = setOf("Author")
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "1111-2222-3333-4444")
        )

        statementService.create(
            subject = resourceService.createResource(
                id = "R4567",
                label = "Author with orcid",
                classes = setOf("Author")
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "0000-1111-2222-3333")
        )

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            createdBy = contributorId,
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
        )

        observatoryService.createObservatory(
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
            researchField = ThingId("R12"),
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
        )
    }

    @AfterEach
    fun cleanup() {
        statementService.removeAll()
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        observatoryService.removeAll()
        organizationService.removeAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun create() {
        post("/api/visualizations")
            .content(requestJson("orkg/createVisualization"))
            .accept(VISUALIZATION_JSON_V2)
            .contentType(VISUALIZATION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
    }
}
