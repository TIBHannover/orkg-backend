package eu.tib.orkg.prototype.ranking.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.ranking.spi.RankingService
import eu.tib.orkg.prototype.ranking.spi.rankingServiceContract
import eu.tib.orkg.prototype.statements.adapter.output.facade.ListAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jClassAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jLiteralAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jPredicateAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jResourceAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jStatementAdapter
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.SpringDataNeo4jThingAdapter
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@EnableCaching
@ContextConfiguration(
    classes = [
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jThingAdapter::class,
        ListAdapter::class,
        SpringDataNeo4jRankingServiceAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jRankingServiceAdapterContractTests(
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val listAdapter: ListRepository,
    @Autowired private val springDataNeo4jRankingServiceAdapter: RankingService
) : DescribeSpec({
    include(
        rankingServiceContract(
            springDataNeo4jStatementAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jLiteralAdapter,
            springDataNeo4jResourceAdapter,
            springDataNeo4jPredicateAdapter,
            listAdapter,
            springDataNeo4jRankingServiceAdapter
        )
    )
})
