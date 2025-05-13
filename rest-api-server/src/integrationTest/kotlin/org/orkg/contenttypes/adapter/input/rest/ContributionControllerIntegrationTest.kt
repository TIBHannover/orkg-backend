package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.createClasses
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class ContributionControllerIntegrationTest : MockMvcBaseTest("contributions") {
    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.findAll()).hasSize(0)
        assertThat(organizationService.findAllConferences()).hasSize(0)

        predicateService.createPredicates(
            Predicates.hasContribution,
            Predicates.description,
            Predicates.hasListElement,
            Predicates.hasEvaluation,
            Predicates.hasResearchProblem
        )

        classService.createClasses(
            Classes.paper,
            Classes.contribution,
        )

        // Example specific entities

        classService.createClasses(
            ThingId("C123"),
            ThingId("Result")
        )

        resourceService.createResource(id = ThingId("R3003"), label = "Some resource")
        resourceService.createResource(id = ThingId("R3004"), label = "Some other resource")
    }

    @AfterEach
    fun cleanup() {
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
        observatoryService.deleteAll()
        organizationService.deleteAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndFetch() {
        val paperId = resourceService.createResource(
            id = ThingId("R165487"),
            label = "Some other resource",
            classes = setOf(Classes.paper)
        )

        val id = post("/api/papers/$paperId/contributions")
            .content(requestJson("orkg/createContribution"))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val contribution = get("/api/contributions/{id}", id)
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, ContributionRepresentation::class.java) }

        contribution.asClue {
            it.id shouldBe id
            it.label shouldBe "Contribution 1"
            it.classes shouldBe setOf(Classes.contribution)
            it.properties shouldNotBe null
            it.properties[Predicates.hasEvaluation].asClue { property ->
                property shouldNotBe null
                property!!.size shouldBe 2
                property shouldContain ThingId("R3004")
            }
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
        }
    }
}
