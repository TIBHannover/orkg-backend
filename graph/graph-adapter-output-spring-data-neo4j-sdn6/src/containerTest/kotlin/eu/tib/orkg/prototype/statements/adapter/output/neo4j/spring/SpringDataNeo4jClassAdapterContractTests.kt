package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import ac.simons.neo4j.migrations.springframework.boot.autoconfigure.MigrationsAutoConfiguration
import eu.tib.orkg.prototype.testing.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.classRepositoryContract
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.neo4j.cypherdsl.core.Functions.labels
import org.orkg.statements.testing.createClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jStatementAdapter::class,
        SpringDataNeo4jLiteralAdapter::class,
        SpringDataNeo4jPredicateAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(value = [Neo4jConfiguration::class])
@ImportAutoConfiguration(MigrationsAutoConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring"])
internal class SpringDataNeo4jClassAdapterContractTests(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jStatementAdapter: StatementRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val neo4jClient: Neo4jClient
) : DescribeSpec({
    include(
        classRepositoryContract(
            springDataNeo4jClassAdapter,
            springDataNeo4jStatementAdapter,
            springDataNeo4jLiteralAdapter,
            springDataNeo4jPredicateAdapter
        )
    )
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
                .withProperties("id", literalOf<String>(`class`.id.value))
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
