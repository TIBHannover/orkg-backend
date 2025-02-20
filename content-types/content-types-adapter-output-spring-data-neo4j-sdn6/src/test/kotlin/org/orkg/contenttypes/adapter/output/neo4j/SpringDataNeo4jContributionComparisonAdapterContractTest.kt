package org.orkg.contenttypes.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.contenttypes.output.testing.fixtures.contributionComparisonRepositoryContract
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
        SpringDataNeo4jContributionComparisonAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class,
    ],
)
internal class SpringDataNeo4jContributionComparisonAdapterContractTest(
    @Autowired private val springDataNeo4jContributionComparisonAdapter: ContributionComparisonRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
) : DescribeSpec({
        include(
            contributionComparisonRepositoryContract(
                springDataNeo4jContributionComparisonAdapter,
                springDataNeo4jStatementAdapter,
                springDataNeo4jClassAdapter,
                springDataNeo4jLiteralAdapter,
                springDataNeo4jResourceAdapter,
                springDataNeo4jPredicateAdapter
            )
        )

        finalizeSpec {
            springDataNeo4jStatementAdapter.deleteAll()
            springDataNeo4jClassAdapter.deleteAll()
            springDataNeo4jLiteralAdapter.deleteAll()
            springDataNeo4jResourceAdapter.deleteAll()
            springDataNeo4jPredicateAdapter.deleteAll()
        }
    })
