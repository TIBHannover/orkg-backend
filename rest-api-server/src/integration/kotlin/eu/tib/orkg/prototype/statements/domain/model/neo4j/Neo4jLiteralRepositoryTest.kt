package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient

@DataNeo4jTest
class Neo4jLiteralRepositoryTest(
    neo4jClient: Neo4jClient
) : Neo4jTestContainersBaseTest(neo4jClient) {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var literalRepository: Neo4jLiteralRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementRepository

    @BeforeEach
    fun setup() {
        resourceRepository.deleteAll()
        literalRepository.deleteAll()
        statementRepository.deleteAll()

        assertThat(resourceRepository.findAll()).hasSize(0)
        assertThat(literalRepository.findAll()).hasSize(0)
        assertThat(statementRepository.findAll()).hasSize(0)
    }

    @Test
    @DisplayName("should save and retrieve statement")
    fun shouldSaveAndRetrieveStatement() {
        Neo4jLiteral(label = "irrelevant", literalId = LiteralId(1))
            .persist()

        val result = literalRepository.findAll()

        assertThat(result).hasSize(1)
    }

    @Test
    @DisplayName("should create connection between resource and literal")
    fun shouldCreateConnectionBetweenResourceAndLiteral() {
        val sub = resourceRepository.save(
            Neo4jResource(
                label = "subject",
                resourceId = ResourceId(1)
            )
        )
        val obj = literalRepository.save(
            Neo4jLiteral(
                label = "object",
                literalId = LiteralId(1)
            )
        )

        statementRepository.save(
            Neo4jStatement(
                statementId = StatementId(23), // irrelevant
                subject = sub,
                `object` = obj,
                predicateId = PredicateId(42) // irrelevant
            )
        )

        assertThat(statementRepository.findAll()).hasSize(1) // TODO: Extract into separate test
    }

    fun Neo4jLiteral.persist(): Neo4jLiteral = literalRepository.save(this)
}
