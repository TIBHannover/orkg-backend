package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.createClasses
import org.orkg.createLiteral
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class ContributionComparisonControllerIntegrationTest : MockMvcBaseTest("contribution-comparison") {
    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    protected lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        resourceService.deleteAll()
        predicateService.deleteAll()
        statementService.deleteAll()
        classService.deleteAll()

        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        predicateService.createPredicates(
            Predicates.yearPublished,
            Predicates.hasContribution,
        )

        classService.createClasses(
            Classes.paper,
            Classes.contribution,
        )
    }

    @Test
    fun fetchContributionInformation() {
        val cont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")
        val cont2 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 2")

        val paper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val paper2 = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")

        val year1 = literalService.createLiteral(label = "2022")
        val year2 = literalService.createLiteral(label = "2023")

        statementService.createStatement(paper1, Predicates.hasContribution, cont1)
        statementService.createStatement(paper2, Predicates.hasContribution, cont2)

        statementService.createStatement(paper1, Predicates.yearPublished, year1)
        statementService.createStatement(paper2, Predicates.yearPublished, year2)

        val ids = listOf(cont1, cont2)

        get("/api/contribution-comparisons/contributions")
            .param("ids", *ids.map(ThingId::value).toTypedArray())
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(ids.size)))
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
    }

    @Test
    fun passingLessThanTwoIDsShouldRaiseAnError() {
        val cont1 = resourceService.createResource(setOf(Classes.contribution), label = "Contribution 1")

        val paper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")

        val year1 = literalService.createLiteral(label = "2022")

        statementService.createStatement(paper1, Predicates.hasContribution, cont1)

        statementService.createStatement(paper1, Predicates.yearPublished, year1)

        val ids = listOf(cont1)

        get("/api/contribution-comparisons/contributions")
            .param("ids", *ids.map(ThingId::value).toTypedArray())
            .perform()
            .andExpect(status().isBadRequest)
    }
}
