package org.orkg.contenttypes.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.contenttypes.adapter.output.neo4j.configuration.ContentTypesNeo4jConfiguration
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.contenttypes.output.testing.fixtures.rosettaStoneStatementRepositoryContract
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jClassAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jLiteralAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jPredicateAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jResourceAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jStatementAdapter
import org.orkg.graph.adapter.output.neo4j.SpringDataNeo4jThingAdapter
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.annotations.Neo4jContainerUnitTest
import org.orkg.testing.configuration.FixedClockConfig

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jRosettaStoneStatementAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jThingAdapter::class,
        GraphNeo4jConfiguration::class,
        ContentTypesNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class,
        FixedClockConfig::class,
    ],
)
internal class SpringDataNeo4jRosettaStoneStatementAdapterContractTest(
    private val springDataNeo4jRosettaStoneStatementAdapter: RosettaStoneStatementRepository,
    private val springDataNeo4jResourceAdapter: ResourceRepository,
    private val springDataNeo4jPredicateAdapter: PredicateRepository,
    private val springDataNeo4jClassAdapter: ClassRepository,
    private val springDataNeo4jLiteralAdapter: LiteralRepository,
    private val springDataNeo4jStatementAdapter: StatementRepository,
) : DescribeSpec({
        include(
            rosettaStoneStatementRepositoryContract(
                springDataNeo4jRosettaStoneStatementAdapter,
                springDataNeo4jResourceAdapter,
                springDataNeo4jPredicateAdapter,
                springDataNeo4jClassAdapter,
                springDataNeo4jLiteralAdapter,
                springDataNeo4jStatementAdapter,
            ),
        )
    })
