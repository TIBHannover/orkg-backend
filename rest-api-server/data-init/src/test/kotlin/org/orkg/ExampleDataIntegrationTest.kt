package org.orkg

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.community.output.ContributorRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryClassRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryGraph
import org.orkg.graph.adapter.output.inmemory.InMemoryLiteralRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryPredicateRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryResourceRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryStatementRepository
import org.orkg.graph.adapter.output.inmemory.InMemoryThingRepository
import org.orkg.graph.domain.ClassService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.PredicateService
import org.orkg.graph.domain.ResourceService
import org.orkg.graph.domain.StatementService
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.fixedClock
import org.springframework.data.domain.PageRequest

@DisplayName("Example Data")
class ExampleDataIntegrationTest {

    private val classHierarchyRepository: ClassHierarchyRepository = mockk()
    private val contributorRepository: ContributorRepository = mockk()

    private val inMemoryGraph: InMemoryGraph = InMemoryGraph()

    private val classRepository: ClassRepository = InMemoryClassRepository(inMemoryGraph)
    private val literalRepository: LiteralRepository = InMemoryLiteralRepository(inMemoryGraph)
    private val predicateRepository: PredicateRepository = InMemoryPredicateRepository(inMemoryGraph)
    private val resourceRepository: ResourceRepository = InMemoryResourceRepository(inMemoryGraph)
    private val statementRepository: StatementRepository = InMemoryStatementRepository(inMemoryGraph)
    private val thingRepository: ThingRepository = InMemoryThingRepository(inMemoryGraph)

    private val classService: ClassService = ClassService(classRepository, fixedClock)
    private val predicateService: PredicateService = PredicateService(
        repository = predicateRepository,
        statementRepository = statementRepository,
        contributorRepository = contributorRepository,
        clock = fixedClock,
    )
    private val resourceService: ResourceService = ResourceService(
        repository = resourceRepository,
        statementRepository = statementRepository,
        classRepository = classRepository,
        classHierarchyRepository = classHierarchyRepository,
        contributorRepository = contributorRepository,
        clock = fixedClock,
    )
    private val statementService: StatementService = StatementService(
        thingRepository = thingRepository,
        predicateService = predicateRepository,
        statementRepository = statementRepository,
        literalRepository = literalRepository,
        clock = fixedClock,
    )

    private val exampleData = ExampleData(resourceService, predicateService, statementService, classService)

    @BeforeEach
    fun setup() {
        classRepository.save(createClass().copy(id = ThingId("ResearchField"), label = "Research Field"))
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
        exampleData.run(null)
        assertThat(statementService.totalNumberOfStatements() > 0)
    }

    @Test
    fun `research fields are typed correctly`() {
        exampleData.run(null)
        assertThat(
            resourceService.findAll(
                includeClasses = setOf(Classes.researchField),
                pageable = PageRequest.of(0, 10)
            ).all { Classes.researchField in it.classes }
        )
    }
}
