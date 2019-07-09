package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
class Neo4jResourceRepositoryTest {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementWithResourceRepository

    @Test
    @DisplayName("should not return null for resources list if none are defined")
    fun shouldNotReturnNullForResourcesListIfNoneAreDefined() {
        Neo4jResource(
            label = "irrelevant",
            resourceId = ResourceId(1)
        ).persist()

        val result = resourceRepository.findAll()

        assertThat(result).hasSize(1)
        assertThat(result.first().resources).isNotNull
    }

    @Test
    @DisplayName("should create connection between two resources")
    fun shouldCreateConnectionBetweenTwoResources() {
        val sub = resourceRepository.save(
            Neo4jResource(
                label = "subject",
                resourceId = ResourceId(1)
            )
        )
        val obj = resourceRepository.save(
            Neo4jResource(
                label = "object",
                resourceId = ResourceId(2)
            )
        )

        // Act

        statementRepository.save(
            Neo4jStatementWithResource(
                statementId = StatementId(23), // irrelevant
                subject = sub,
                `object` = obj,
                predicateId = PredicateId(42) // irrelevant
            )
        )

        assertThat(statementRepository.findAll()).hasSize(1) // TODO: Extract into separate test

        // Assert

        val allFound = resourceRepository.findAllByLabelMatchesRegex("subject") // TODO: See declaration

        assertThat(allFound).isNotEmpty
        assertThat(allFound).hasSize(1)

        val found = allFound.first()

        assertThat(found.resources).isNotNull
        assertThat(found.resources).isNotEmpty
        assertThat(found.resources).hasSize(1)
        assertThat(found.resources.first().`object`?.label).isEqualTo("object")
    }

    fun Neo4jResource.persist(): Neo4jResource = resourceRepository.save(this)
}
