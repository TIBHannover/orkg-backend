package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.predicateRepositoryContract
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@Ignored("Workaround for Docker container issue active (\"all in one\"). Remove when solved.")
@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jPredicateAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jPredicateAdapterContractTests(
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository
) : DescribeSpec({
    include(predicateRepositoryContract(springDataNeo4jPredicateAdapter))
})
