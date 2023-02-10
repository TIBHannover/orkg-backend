package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.Neo4jContainerInitializer
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.statements.testing.createClass
import org.orkg.statements.testing.createLiteral
import org.orkg.statements.testing.createPredicate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.test.context.ContextConfiguration

@DataNeo4jTest
@ContextConfiguration(
    classes = [
        SpringDataNeo4jClassAdapter::class,
        SpringDataNeo4jPredicateAdapter::class,
        SpringDataNeo4jLiteralAdapter::class
    ],
    initializers = [
        Neo4jContainerInitializer::class
    ]
)
@Import(Neo4jConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring"])
internal class DelegatedDescriptionTest(
    @Autowired private val springDataNeo4jClassAdapter: ClassRepository,
    @Autowired private val springDataNeo4jPredicateAdapter: PredicateRepository,
    @Autowired private val springDataNeo4jLiteralAdapter: LiteralRepository,
    @Autowired private val neo4jClient: Neo4jClient,
) : DescribeSpec({
    include(describeSpec {
        describe("loading a class") {
            it("returns the correct result") {
                val subject = createClass()
                val predicate = createPredicate(
                    id = PredicateId("description")
                )
                val `object` = createLiteral(
                    label = "Unique class description"
                )
                springDataNeo4jClassAdapter.save(subject)
                springDataNeo4jPredicateAdapter.save(predicate)
                springDataNeo4jLiteralAdapter.save(`object`)

                neo4jClient.query("""MATCH (s {class_id: "${subject.id}"}), (o {literal_id: "${`object`.id}"})
                CREATE (s)-[r:RELATED {predicate_id: "${predicate.id}", statement_id: "S1"}]->(o)
                """.trimMargin()).run()

                val result = springDataNeo4jClassAdapter.findByClassId(subject.id)

                result shouldNotBe null
                result.isPresent shouldBe true
                result.get().asClue {
                    it.description shouldBe `object`.label
                }
            }
        }
        describe("loading a predicate") {
            it("returns the correct result") {
                val subject = createPredicate()
                val predicate = createPredicate(
                    id = PredicateId("description")
                )
                val `object` = createLiteral(
                    label = "Unique class description"
                )
                springDataNeo4jPredicateAdapter.save(subject)
                springDataNeo4jPredicateAdapter.save(predicate)
                springDataNeo4jLiteralAdapter.save(`object`)

                neo4jClient.query("""MATCH (s {class_id: "${subject.id}"}), (o {literal_id: "${`object`.id}"})
                CREATE (s)-[r:RELATED {predicate_id: "${predicate.id}", statement_id: "S1"}]->(o)
                """.trimMargin()).run()

                val result = springDataNeo4jPredicateAdapter.findByPredicateId(subject.id)

                result shouldNotBe null
                result.isPresent shouldBe true
                result.get().asClue {
                    it.description shouldBe `object`.label
                }
            }
        }
    })
})
