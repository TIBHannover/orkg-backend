package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.IndexInitializer
import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.resourceRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jResourceAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(value = [Neo4jConfiguration::class, IndexInitializer::class])
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jResourceAdapterContractTests(
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository
) : DescribeSpec({
    include(resourceRepositoryContract(springDataNeo4jResourceAdapter))
})