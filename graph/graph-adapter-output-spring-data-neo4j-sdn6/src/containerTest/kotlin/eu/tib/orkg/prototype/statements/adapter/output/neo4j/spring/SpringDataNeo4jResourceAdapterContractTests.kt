package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import eu.tib.orkg.prototype.testing.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.resourceRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Functions.labels
import org.orkg.statements.testing.createResource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jResourceAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(value = [Neo4jConfiguration::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jResourceAdapterContractTests(
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val neo4jClient: Neo4jClient
) : DescribeSpec({
    include(resourceRepositoryContract(springDataNeo4jResourceAdapter))
    include(neo4jResourceRepositoryContract(springDataNeo4jResourceAdapter, neo4jClient))
})

fun <R : ResourceRepository, C : Neo4jClient> neo4jResourceRepositoryContract(
    repository: R,
    neo4jClient: C
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    describe("saving a resource") {
        it("saves the correct labels") {
            val resource = createResource()
            repository.save(resource)

            val n = anyNode()
                .named("n")
                .withProperties("id", literalOf<String>(resource.id.value))
            val query = match(n)
                .unwind(labels(n))
                .`as`("label")
                .returning("label")
                .build()

            val actual = neo4jClient.query(query.cypher)
                .fetchAs<String>()
                .all()

            actual shouldNotBe null
            actual.size shouldBe 2
            actual shouldContainAll setOf("Resource", "Thing")
        }
    }
}
