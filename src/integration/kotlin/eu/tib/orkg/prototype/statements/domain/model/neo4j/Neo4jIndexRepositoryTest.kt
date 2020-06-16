package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.Neo4jRepositoryTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Neo4jRepositoryTest
class Neo4jIndexRepositoryTest {

    @Autowired
    private lateinit var indexRepository: Neo4jIndexRepository

    @Test
    @DisplayName("should create unique index w/o exception")
    fun shouldCreateUniqueIndexShouldWork() {
        indexRepository.createUniqueConstraint("Resource", "label")
    }

    @Test
    @DisplayName("should create property index w/o exception")
    fun shouldCreatePropertyIndexShouldWork() {
        indexRepository.createPropertyIndex("Resource", "label")
    }
}
