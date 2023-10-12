package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.services.ClassService
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.statements.services.ResourceService
import eu.tib.orkg.prototype.statements.services.StatementService
import eu.tib.orkg.prototype.testing.annotations.Neo4jContainerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@Tag("development")
@DisplayName("Example Data")
@Transactional
@Neo4jContainerIntegrationTest
class ExampleDataTest {

    @Autowired
    private lateinit var resourceService: ResourceService

    @Autowired
    private lateinit var predicateService: PredicateService

    @Autowired
    private lateinit var statementService: StatementService

    @Autowired
    private lateinit var classService: ClassService

    @BeforeEach
    fun setup() {
        cleanup()
        ExampleData(resourceService, predicateService, statementService, classService).run(null)
    }

    @AfterEach
    fun cleanup() {
        val tempPageable = PageRequest.of(0, 10)

        predicateService.removeAll()
        resourceService.removeAll()
        statementService.removeAll()
        classService.removeAll()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(statementService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
    }

    @Test
    fun `example data is created and statements exist in the graph`() {
        assertThat(statementService.totalNumberOfStatements() > 0)
    }

    @Test
    fun `research fields are typed correctly`() {
        assertThat(
            resourceService.findAllByClass(PageRequest.of(0, 10), ThingId("ResearchField"))
                .all { ThingId("ResearchField") in it.classes }
        )
    }
}
