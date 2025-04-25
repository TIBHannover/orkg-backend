package org.orkg.graph.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.thingRepositoryContract
import org.orkg.testing.annotations.Neo4jContainerUnitTest
import org.springframework.beans.factory.annotation.Autowired

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jThingAdapter::class,
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        GraphNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class
    ]
)
internal class SpringDataNeo4jThingAdapterContractTest(
    @Autowired private val springDataNeo4jThingAdapter: ThingRepository,
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
) : DescribeSpec({
        include(
            thingRepositoryContract(
                springDataNeo4jThingAdapter,
                springDataNeo4jClassAdapter,
                springDataNeo4jResourceAdapter,
                springDataNeo4jPredicateAdapter,
                springDataNeo4jLiteralAdapter,
            )
        )

        finalizeSpec {
            springDataNeo4jClassAdapter.deleteAll()
            springDataNeo4jResourceAdapter.deleteAll()
            springDataNeo4jPredicateAdapter.deleteAll()
            springDataNeo4jLiteralAdapter.deleteAll()
        }
    })
