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
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.testing.fixtures.classRepositoryContract
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.annotations.Neo4jContainerUnitTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.fetchAs

@Neo4jContainerUnitTest(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        GraphNeo4jConfiguration::class,
        CypherQueryBuilderConfiguration::class
    ]
)
internal class SpringDataNeo4jClassAdapterContractTests(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val neo4jClient: Neo4jClient,
) : DescribeSpec({
        include(
            classRepositoryContract(
                springDataNeo4jClassAdapter
            )
        )
        include(neo4jClassRepositoryContract(springDataNeo4jClassAdapter, neo4jClient))

        finalizeSpec {
            springDataNeo4jClassAdapter.deleteAll()
        }
    })

fun <R : ClassRepository, C : Neo4jClient> neo4jClassRepositoryContract(
    repository: R,
    neo4jClient: C,
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
