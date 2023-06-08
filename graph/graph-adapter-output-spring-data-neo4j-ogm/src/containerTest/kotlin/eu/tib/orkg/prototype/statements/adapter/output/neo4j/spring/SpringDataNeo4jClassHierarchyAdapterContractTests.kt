package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.classHierarchyRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jClassHierarchyAdapter::class,
        SpringDataNeo4jClassRelationAdapter::class,
        SpringDataNeo4jResourceAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
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
})
