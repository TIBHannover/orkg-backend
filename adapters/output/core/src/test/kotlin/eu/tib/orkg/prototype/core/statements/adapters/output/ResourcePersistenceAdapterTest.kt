package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepositoryContract
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient


@DataNeo4jTest
@DisplayName("An adapter for a ResourceRepository")
internal class ResourcePersistenceAdapterTest(
    private val adapterUnderTest: ResourcePersistenceAdapter,
    neo4jClient: Neo4jClient
) : Neo4jTestContainersBaseTest(neo4jClient), ResourceRepositoryContract {
    override val repository: ResourceRepository
        get() = adapterUnderTest
}
