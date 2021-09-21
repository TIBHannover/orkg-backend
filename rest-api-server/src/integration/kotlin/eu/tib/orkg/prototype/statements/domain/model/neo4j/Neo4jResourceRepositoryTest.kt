package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.neo4j.core.Neo4jClient

@DataNeo4jTest
class Neo4jResourceRepositoryTest(
    neo4jClient: Neo4jClient
) : Neo4jTestContainersBaseTest(neo4jClient) {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementRepository

    @BeforeEach
    fun setup() {
        resourceRepository.deleteAll()
        statementRepository.deleteAll()

        assertThat(resourceRepository.findAll()).hasSize(0)
        assertThat(statementRepository.findAll()).hasSize(0)
    }

    @Test
    @DisplayName("should show is shared resource")
    fun shouldShowIsSharedResource() {
        val sub = Neo4jResource(
            label = "subject",
            resourceId = ResourceId(1)
        ).persist()
        val sub2 = Neo4jResource(
            label = "subject2",
            resourceId = ResourceId(2)
        ).persist()
        val obj = Neo4jResource(
            label = "object",
            resourceId = ResourceId(3)
        ).persist()
        val obj2 = Neo4jResource(
            label = "object2",
            resourceId = ResourceId(4)
        ).persist()

        // Act

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(23), // irrelevant
                subject = sub,
                `object` = obj,
                predicateId = PredicateId(42) // irrelevant
            )
        )

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(24), // irrelevant
                subject = sub2,
                `object` = obj,
                predicateId = PredicateId(43) // irrelevant
            )
        )

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(25), // irrelevant
                subject = obj,
                `object` = obj2,
                predicateId = PredicateId(44) // irrelevant
            )
        )

        val result = resourceRepository.findByResourceId(obj.resourceId)
        fail { "Needs fixing, as property is not available anymore." }
        // assertThat(result.get().objectOf).hasSize(2)
    }

    @Test
    fun testFindingClasses() {
        val pagination = PageRequest.of(0, 10)

        val resourceToBeFound = Neo4jResource("tiger", ResourceId("R1")).also { it.assignTo("C0") }
        resourceRepository.save(resourceToBeFound)

        // with different class
        resourceRepository.save(Neo4jResource("cat", ResourceId("R2")).also { it.assignTo("C99") })

        // without class
        resourceRepository.save(Neo4jResource("cat", ResourceId("R2")))

        val result = resourceRepository.findAllByClass("C0", pagination)

        assertThat(result).hasSize(1)
        assertThat(result).containsExactlyInAnyOrder(resourceToBeFound)
        assertThat(result.first().classes).containsExactlyInAnyOrder(ClassId("C0"))
    }

    fun Neo4jResource.persist(): Neo4jResource = resourceRepository.save(this)
}
