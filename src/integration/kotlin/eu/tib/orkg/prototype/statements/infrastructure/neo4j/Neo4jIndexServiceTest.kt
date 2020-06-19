package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.Neo4jServiceTest
import eu.tib.orkg.prototype.statements.domain.model.IndexService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jIndexRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Neo4jServiceTest
@DisplayName("Neo4j: Index service")
class Neo4jIndexServiceTest {

    @Autowired
    private lateinit var service: IndexService

    @Autowired
    private lateinit var repository: Neo4jIndexRepository

    @Test
    @DisplayName("should create required indices")
    fun shouldCreateNeededIndices() {
        service.verifyIndices()

        // 12 here is hard coded because we have also 12 constraints hardcoded
        assertThat(repository.getExistingIndicesAndConstraints().count() >= 12)
    }
}
