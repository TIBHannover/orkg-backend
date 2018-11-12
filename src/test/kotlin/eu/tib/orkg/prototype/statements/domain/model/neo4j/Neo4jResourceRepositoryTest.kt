package eu.tib.orkg.prototype.statements.domain.model.neo4j

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
class Neo4jResourceRepositoryTest {

    @Autowired
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Autowired
    private lateinit var statementRepository: Neo4jStatementWithResourceRepository

    @Test
    @DisplayName("should not return null for resources list if none are defined")
    fun shouldNotReturnNullForResourcesListIfNoneAreDefined() {
        Neo4jResource(label = "irrelevant").persist()

        val result = resourceRepository.findAll()

        assertThat(result).hasSize(1)
        assertThat(result.first().resources).isNotNull
    }

    @Test
    @DisplayName("should create connection between two resources")
    fun shouldCreateConnectionBetweenTwoResources() {
        val sub = resourceRepository.save(Neo4jResource(label = "subject"))
        val obj = resourceRepository.save(Neo4jResource(label = "object"))

        // Act

        statementRepository.save(
            Neo4jStatementWithResource(
                subject = sub,
                `object` = obj,
                predicateId = 42 // irrelevant
            )
        )

        assertThat(statementRepository.findAll()).hasSize(1) // TODO: Extract into separate test

        // Assert

        val allFound = resourceRepository.findAllByLabel("subject")

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
