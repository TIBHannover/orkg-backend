package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.classRelationRepositoryContract
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
        SpringDataNeo4jClassRelationAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jClassRelationAdapterContractTests(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jClassRelationAdapter: ClassRelationRepository,
    @Autowired private val springDataNeo4jClassHierarchyAdapter: ClassHierarchyRepository
) : DescribeSpec({
    include(
        classRelationRepositoryContract(
            springDataNeo4jClassRelationAdapter,
            springDataNeo4jClassAdapter,
            springDataNeo4jClassHierarchyAdapter
        )
    )
})
