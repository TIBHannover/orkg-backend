package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.predicateRepositoryContract
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
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createPredicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs
import org.springframework.test.context.ContextConfiguration

@Ignored("Workaround for Docker container issue active (\"all in one\"). Remove when solved.")
@DataNeo4jTest
@ContextConfiguration(classes = [SpringDataNeo4jPredicateAdapter::class], initializers = [Neo4jContainerInitializer::class])
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class SpringDataNeo4jPredicateAdapterContractTests(
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val neo4jClient: Neo4jClient
) : DescribeSpec({
    include(predicateRepositoryContract(springDataNeo4jPredicateAdapter))
    include(neo4jPredicateRepositoryContract(springDataNeo4jPredicateAdapter, neo4jClient))
})

fun <R : PredicateRepository, C : Neo4jClient> neo4jPredicateRepositoryContract(
    repository: R,
    neo4jClient: C
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    describe("saving a predicate") {
        it("saves the correct labels") {
            val predicate = createPredicate()
            repository.save(predicate)

            val n = anyNode()
                .named("n")
                .withProperties("predicate_id", literalOf<String>(predicate.id.value))
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
            actual shouldContainAll setOf("Predicate", "Thing")
        }
    }
}