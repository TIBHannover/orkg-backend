package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.classRepositoryContract
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.exist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.*
import org.neo4j.cypherdsl.core.Functions
import org.neo4j.cypherdsl.core.Functions.*
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.withCustomMappings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.ContextConfiguration

@Ignored("Workaround for Docker container issue active (\"all in one\"). Remove when solved.")
@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jClassAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring"])
internal class SpringDataNeo4jClassAdapterContractTests(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val neo4jClient: Neo4jClient
) : DescribeSpec({
    include(classRepositoryContract(springDataNeo4jClassAdapter))
    include(neo4jClassRepositoryContract(springDataNeo4jClassAdapter, neo4jClient))
})

fun <R : ClassRepository, C : Neo4jClient> neo4jClassRepositoryContract(
    repository: R,
    neo4jClient: C
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    describe("saving a class") {
        it("saves the correct labels") {
            val `class` = createClass()
            repository.save(`class`)

            val n = anyNode()
                .named("n")
                .withProperties("class_id", literalOf<String>(`class`.id.value))
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
            actual shouldContainAll setOf("Class", "Thing")
        }
    }
}
