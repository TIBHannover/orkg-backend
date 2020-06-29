package eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j

import eu.tib.orkg.prototype.Neo4jRepositoryTest
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.Neo4jIndexRepository
import eu.tib.orkg.prototype.graphdb.indexing.domain.model.neo4j.PropertyIndex
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Neo4jRepositoryTest
class Neo4jIndexRepositoryTest {

    @Autowired
    private lateinit var indexRepository: Neo4jIndexRepository

    @Test
    @DisplayName("should create unique index w/o exception")
    fun shouldCreateIndex() {
        indexRepository.createIndex(
            PropertyIndex(
                "Resource",
                "label"
            )
        )
    }
}
