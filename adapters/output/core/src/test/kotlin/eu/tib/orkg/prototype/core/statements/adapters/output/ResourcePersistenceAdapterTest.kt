package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.statements.ports.ResourceRepository
import eu.tib.orkg.prototype.statements.ports.ResourceRepositoryContract
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest


@DataNeo4jTest
@DisplayName("An adapter for a ResourceRepository")
internal class ResourcePersistenceAdapterTest(
    private val adapterUnderTest: ResourcePersistenceAdapter,
) : Neo4jTestContainersBaseTest(), ResourceRepositoryContract {
    override fun cleanup() = Unit

    override val repository: ResourceRepository
        get() = adapterUnderTest
}
