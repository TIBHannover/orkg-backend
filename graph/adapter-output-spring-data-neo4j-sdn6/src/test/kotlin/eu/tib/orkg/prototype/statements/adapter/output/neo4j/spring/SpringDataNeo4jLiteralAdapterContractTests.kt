package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.literalRepositoryContract
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Cypher.*
import org.neo4j.cypherdsl.core.Functions
import org.neo4j.cypherdsl.core.Functions.*
import org.orkg.statements.testing.createLiteral
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
internal class SpringDataNeo4jLiteralAdapterContractTests(
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val neo4jClient: Neo4jClient
) : DescribeSpec({
    include(literalRepositoryContract(springDataNeo4jLiteralAdapter))
    include(neo4jLiteralRepositoryContract(springDataNeo4jLiteralAdapter, neo4jClient))
})

fun <R : LiteralRepository, C : Neo4jClient> neo4jLiteralRepositoryContract(
    repository: R,
    neo4jClient: C
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    describe("saving a literal") {
        it("saves the correct labels") {
            val literal = createLiteral()
            repository.save(literal)

            val n = anyNode()
                .named("n")
                .withProperties("literal_id", literalOf<String>(literal.id.value))
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
            actual shouldContainAll setOf("Literal", "Thing")
        }
    }
}
