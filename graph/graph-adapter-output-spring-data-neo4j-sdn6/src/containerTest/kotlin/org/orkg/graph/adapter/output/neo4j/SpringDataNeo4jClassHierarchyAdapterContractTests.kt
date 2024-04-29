package org.orkg.graph.adapter.output.neo4j

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import io.kotest.core.spec.style.DescribeSpec
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.classHierarchyRepositoryContract
import org.orkg.graph.testing.fixtures.classRelationRepositoryContract
import org.orkg.testing.Neo4jContainerInitializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jClassHierarchyAdapter::class,
        SpringDataNeo4jResourceAdapter::class,
        GraphNeo4jConfiguration::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
internal class SpringDataNeo4jClassHierarchyAdapterContractTests(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jClassRelationAdapter: ClassRelationRepository,
    @Autowired private val springDataNeo4jClassHierarchyAdapter: ClassHierarchyRepository,
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository
) : DescribeSpec({
    include(
        classHierarchyRepositoryContract(
            springDataNeo4jClassHierarchyAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jClassRelationAdapter,
            springDataNeo4jResourceAdapter
        )
    )
    include(
        classRelationRepositoryContract(
            springDataNeo4jClassRelationAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jClassHierarchyAdapter
        )
    )

    finalizeSpec {
        springDataNeo4jClassRelationAdapter.deleteAll()
        springDataNeo4jResourceAdapter.deleteAll()
        springDataNeo4jClassAdapter.deleteAll()
    }
})
