package org.orkg.contenttypes.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.contenttypes.output.testing.fixtures.researchFieldRepositoryContract
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jClassAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jLiteralAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jPredicateAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jResourceAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jStatementAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.annotations.Neo4jContainerUnitTest
import org.springframework.beans.factory.annotation.Autowired

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jResearchFieldAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class
    ]
)
internal class SpringDataNeo4jResearchFieldAdapterContractTest(
    @param:Autowired private val springDataNeo4jResearchFieldAdapter: ResearchFieldRepository,
    @param:Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @param:Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @param:Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @param:Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @param:Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
) : DescribeSpec({
        include(
            researchFieldRepositoryContract(
                springDataNeo4jResearchFieldAdapter,
                springDataNeo4jStatementAdapter,
                springDataNeo4jClassAdapter,
                springDataNeo4jLiteralAdapter,
                springDataNeo4jResourceAdapter,
                springDataNeo4jPredicateAdapter,
            )
        )

        afterSpec {
            springDataNeo4jStatementAdapter.deleteAll()
            springDataNeo4jClassAdapter.deleteAll()
            springDataNeo4jLiteralAdapter.deleteAll()
            springDataNeo4jResourceAdapter.deleteAll()
            springDataNeo4jPredicateAdapter.deleteAll()
        }
    })
