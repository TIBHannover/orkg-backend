package org.orkg.graph.adapter.output.neo4j

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.neo4j.cypherdsl.core.Cypher.anyNode
import org.neo4j.cypherdsl.core.Cypher.labels
import org.neo4j.cypherdsl.core.Cypher.literalOf
import org.neo4j.cypherdsl.core.Cypher.match
import org.orkg.common.neo4jdsl.configuration.CypherQueryBuilderConfiguration
import org.orkg.graph.adapter.output.neo4j.configuration.GraphNeo4jConfiguration
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.literalRepositoryContract
import org.orkg.testing.annotations.Neo4jContainerUnitTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jLiteralAdapter::class,
        GraphNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class
    ]
)
internal class SpringDataNeo4jLiteralAdapterContractTests(
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val neo4jClient: Neo4jClient,
) : DescribeSpec({
        include(literalRepositoryContract(springDataNeo4jLiteralAdapter))
        include(neo4jLiteralRepositoryContract(springDataNeo4jLiteralAdapter, neo4jClient))

        afterSpec {
            springDataNeo4jLiteralAdapter.deleteAll()
        }
    })

fun <R : LiteralRepository, C : Neo4jClient> neo4jLiteralRepositoryContract(
    repository: R,
    neo4jClient: C,
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
                .withProperties("id", literalOf<String>(literal.id.value))
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
