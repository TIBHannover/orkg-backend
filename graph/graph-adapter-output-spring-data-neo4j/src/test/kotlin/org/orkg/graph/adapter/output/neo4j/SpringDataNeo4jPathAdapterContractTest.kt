package org.orkg.graph.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PathRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.pathRepositoryContract
import org.orkg.testing.annotations.Neo4jContainerUnitTest

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jPathAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        GraphNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class,
    ],
)
internal class SpringDataNeo4jPathAdapterContractTest(
    private val springDataNeo4jPathAdapter: PathRepository,
    private val springDataNeo4jStatementAdapter: StatementRepository,
    private val springDataNeo4jClassAdapter: ClassRepository,
    private val springDataNeo4jLiteralAdapter: LiteralRepository,
    private val springDataNeo4jResourceAdapter: ResourceRepository,
    private val springDataNeo4jPredicateAdapter: PredicateRepository,
) : DescribeSpec({
        include(
            pathRepositoryContract(
                springDataNeo4jPathAdapter,
                springDataNeo4jStatementAdapter,
                springDataNeo4jClassAdapter,
                springDataNeo4jLiteralAdapter,
                springDataNeo4jResourceAdapter,
                springDataNeo4jPredicateAdapter,
            ),
        )
    })
