package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatement
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jStatementRepository
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class Neo4jResourceRepositoryTest : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementRepository

    @Autowired
    private lateinit var classRepository: Neo4jClassRepository

    @BeforeEach
    fun setup() {
        resourceRepository.deleteAll()
        statementRepository.deleteAll()
        classRepository.deleteAll()

        assertThat(resourceRepository.findAll()).hasSize(0)
        assertThat(statementRepository.findAll()).hasSize(0)
        assertThat(classRepository.findAll()).hasSize(0)
    }

    @Test
    @DisplayName("should not return null for resources list if none are defined")
    fun shouldNotReturnNullForResourcesListIfNoneAreDefined() {
        Neo4jResource(
            label = "irrelevant", resourceId = ResourceId(1)
        ).persist()

        val result = resourceRepository.findAll()

        assertThat(result).hasSize(1)
    }

    @Test
    @Disabled("Because of #467, Neo4jResource currently does relate to other entities")
    @DisplayName("should show is shared resource")
    fun shouldShowIsSharedResource() {
        val sub = Neo4jResource(
            label = "subject", resourceId = ResourceId(1)
        ).persist()
        val sub2 = Neo4jResource(
            label = "subject2", resourceId = ResourceId(2)
        ).persist()
        val obj = Neo4jResource(
            label = "object", resourceId = ResourceId(3)
        ).persist()
        val obj2 = Neo4jResource(
            label = "object2", resourceId = ResourceId(4)
        ).persist()

        // Act

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(23), // irrelevant
                subject = sub, `object` = obj, predicateId = PredicateId(42) // irrelevant
            )
        )

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(24), // irrelevant
                subject = sub2, `object` = obj, predicateId = PredicateId(43) // irrelevant
            )
        )

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(25), // irrelevant
                subject = obj, `object` = obj2, predicateId = PredicateId(44) // irrelevant
            )
        )

//        val result = resourceRepository.findByResourceId(obj.resourceId)
//        assertThat(result.get().objectOf).hasSize(2)
    }

    @Test
    @Disabled("Because of #467, Neo4jResource currently does relate to other entities")
    @DisplayName("should create connection between two resources")
    fun shouldCreateConnectionBetweenTwoResources() {
        val sub = resourceRepository.save(
            Neo4jResource(
                label = "subject", resourceId = ResourceId(1)
            )
        )
        val obj = resourceRepository.save(
            Neo4jResource(
                label = "object", resourceId = ResourceId(2)
            )
        )

        // Act

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(23),
                subject = sub, `object` = obj, predicateId = PredicateId(42) // irrelevant
            )
        )

        assertThat(statementRepository.findAll()).hasSize(1) // TODO: Extract into separate test

        // Assert

//        val found = resourceRepository.findByResourceId(sub.resourceId)
//
//        assertThat(found.resources).isNotNull
//        assertThat(found.resources).isNotEmpty
//        assertThat(found.resources).hasSize(1)
//        assertThat(found.resources.first().`object`?.label).isEqualTo("object")
    }

    @Test
    fun testFindingClasses() {
        val pagination = PageRequest.of(0, 10)

        val resourceToBeFound = Neo4jResource("tiger", ResourceId("R1")).also { it.assignTo("C0") }
        resourceRepository.save(resourceToBeFound)

        // with different class
        resourceRepository.save(Neo4jResource("cat", ResourceId("R2")).also { it.assignTo("C99") })

        // without class
        resourceRepository.save(Neo4jResource("cat", ResourceId("R3")))

        val result = resourceRepository.findAllByClass(ClassId("C0"), pagination)

        assertThat(result).hasSize(1)
        assertThat(result).containsExactlyInAnyOrder(resourceToBeFound)
        assertThat(result.first().classes).containsExactlyInAnyOrder(ThingId("C0"))
    }

    fun Neo4jResource.persist(): Neo4jResource = resourceRepository.save(this)
}
