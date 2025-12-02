package org.orkg.contenttypes.adapter.input.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.input.LegacyPaperUseCases
import org.orkg.createClasses
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class LegacyPaperControllerIntegrationTest : MockMvcBaseTest("papers") {
    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var legacyPaperService: LegacyPaperUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)

        classService.createClasses(Classes.paper)
    }

    @Test
    fun fetchPapersRelatedToAParticularResource() {
        val predicate1 = predicateService.createPredicate(label = "Predicate 1")
        val predicate2 = predicateService.createPredicate(label = "Predicate 2")

        val relatedPaper1 = resourceService.createResource(setOf(Classes.paper), label = "Paper 1")
        val relatedPaper2 = resourceService.createResource(setOf(Classes.paper), label = "Paper 2")
        val unrelatedPaper = resourceService.createResource(setOf(Classes.paper), label = "Paper 3")
        val intermediateResource = resourceService.createResource(label = "Not interesting")
        val unrelatedResource = resourceService.createResource(label = "Some resource")
        val id = resourceService.createResource(label = "Our resource")

        statementService.createStatement(relatedPaper1, predicate1, id)
        statementService.createStatement(relatedPaper2, predicate2, intermediateResource)
        statementService.createStatement(intermediateResource, predicate1, id)
        statementService.createStatement(unrelatedPaper, predicate1, unrelatedResource)

        get("/api/papers")
            .param("linked_to", "$id")
            .param("size", "50")
            .perform()
            .andExpect(status().isOk)
    }
}
