package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepositoryContractTest
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.springframework.beans.factory.annotation.Autowired

class SpringDataNeo4jResourceAdapterContractTests : Neo4jTestContainersBaseTest(), ResourceRepositoryContractTest {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jResourceAdapter

    override val repository: ResourceRepository
        get() = adapter
}
