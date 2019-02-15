package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit.jupiter.*
import org.springframework.transaction.annotation.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
class Neo4jLiteralRepositoryTest {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var literalRepository: Neo4jLiteralRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementWithLiteralRepository

    @Test
    @DisplayName("should save and retrieve statement")
    fun shouldSaveAndRetrieveStatement() {
        Neo4jLiteral(label = "irrelevant").persist()

        val result = literalRepository.findAll()

        assertThat(result).hasSize(1)
    }

    @Test
    @DisplayName("should create connection between resource and literal")
    fun shouldCreateConnectionBetweenResourceAndLiteral() {
        val sub = resourceRepository.save(Neo4jResource(label = "subject"))
        val obj = literalRepository.save(Neo4jLiteral(label = "object"))

        statementRepository.save(
            Neo4jStatementWithLiteral(
                subject = sub,
                `object` = obj,
                predicateId = PredicateId(42) // irrelevant
            )
        )

        assertThat(statementRepository.findAll()).hasSize(1) // TODO: Extract into separate test
    }

    fun Neo4jLiteral.persist(): Neo4jLiteral = literalRepository.save(this)
}
