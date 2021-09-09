package eu.tib.orkg.prototype.core.statements.adapters.output

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.cypher.asCypherString
import eu.tib.orkg.prototype.statements.domain.model.Class
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

@DataNeo4jTest
@DisplayName("An adapter for a Statement Repository")
internal class StatementPersistenceAdapterTest(
    private val adapterUnderTest: StatementPersistenceAdapter,
    private val neo4jClient: Neo4jClient
) : Neo4jTestContainersBaseTest(neo4jClient) {
    @Test
    fun testFindById() {
        val now = OffsetDateTime.now()
        val uuid = UUID.randomUUID()
        val subject = createResource(now, uuid, setOf(ClassId("C1")))
        val `object` = createClass(now, uuid)
        val statement = GeneralStatement(
            id = StatementId("S1"),
            subject = subject,
            predicate = Predicate(PredicateId("IS_A"), "is a", now, ContributorId(uuid)),
            `object` = `object`,
            createdAt = now,
            createdBy = ContributorId(uuid)
        )
        val query = """
            CREATE ${subject.asCypherString("s")}
            CREATE ${`object`.asCypherString("o")}
            CREATE (p:Thing:Predicate {predicate_id: "IS_A", label: "is a", created_by: "$uuid", created_at: "$now"})
            CREATE (s)-[:RELATED {statement_id: "S1", predicate_id: "IS_A", created_at: "$now", created_by: "$uuid"}]->(o)
            """.trimIndent()
        neo4jClient.query(query).run()

        val actual = adapterUnderTest.findById(StatementId("S1")).orElseThrow()

        assertThat(actual).isEqualTo(statement)
    }

    fun createClass(now: OffsetDateTime, uuid: UUID) =
        Class(
            id = ClassId("C1"),
            label = "some class",
            uri = URI.create("http://example.org/IS_A"),
            createdAt = now,
            createdBy = ContributorId(uuid)
        )

    fun createResource(now: OffsetDateTime, uuid: UUID, classes: Set<ClassId> = emptySet()) =
        Resource(
            id = ResourceId("R1"),
            label = "some resource",
            createdAt = now,
            createdBy = ContributorId(uuid),
            classes = classes,
        )
}
