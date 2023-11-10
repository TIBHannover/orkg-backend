package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.testing.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.classHierarchyRepositoryContract
import eu.tib.orkg.prototype.statements.spi.classRelationRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jClassHierarchyAdapter::class,
        SpringDataNeo4jResourceAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring"])
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
