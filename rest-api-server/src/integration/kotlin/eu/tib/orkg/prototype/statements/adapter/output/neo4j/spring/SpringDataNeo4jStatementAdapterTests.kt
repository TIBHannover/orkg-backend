package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.domain.model.GeneralStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class SpringDataNeo4jStatementAdapterTests : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jStatementAdapter

    @Autowired
    private lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var predicateRepository: PredicateRepository

    @Test
    fun `saving, loading and saving modified works`() {
        val s = createResource().copy(id = ResourceId(1))
        val p = createPredicate()
        val o = createResource().copy(id = ResourceId(2))
        resourceRepository.save(s)
        resourceRepository.save(o)
        predicateRepository.save(p)
        val statement = createStatement(s, p, o)
        adapter.save(statement)

        val found = adapter.findByStatementId(statement.id!!).get()
        val newPredicate = createPredicate().copy(id = PredicateId("NEW_PREDICATE_ID"), label = "other label")
        predicateRepository.save(newPredicate)
        val modified = found.copy(predicate = newPredicate)
        adapter.save(modified)

        assertThat(adapter.findAll(PageRequest.of(0, 10)).totalElements).isEqualTo(1)
        val actual = adapter.findByStatementId(statement.id!!).get()
        assertThat(actual).isEqualTo(modified)
    }

    @Test
    fun `saving, loading, modifying and saving a resource with several statements works`() {
        val s = createResource().copy(id = ResourceId(1))
        val p = createPredicate()
        resourceRepository.save(s)
        predicateRepository.save(p)
        for (i in 0L until 5) {
            val o = createResource().copy(id = ResourceId(100 + i))
            resourceRepository.save(o)
            val statement = createStatement(s, p, o).copy(id = StatementId(i))
            adapter.save(statement)
        }

        // Modify the predicate of the second statement.
        val modifiedId = StatementId(1)
        val found = adapter.findByStatementId(modifiedId).get()
        val newPredicate = createPredicate().copy(id = PredicateId("NEW_PREDICATE_ID"), label = "other label")
        predicateRepository.save(newPredicate)
        val modified = found.copy(predicate = newPredicate)
        adapter.save(modified)

        // Create an "incoming" statement for the subject.
        val si = createResource().copy(id = ResourceId(1000))
        resourceRepository.save(si)
        val pi = createPredicate().copy(id = PredicateId(1000), label = "incoming")
        predicateRepository.save(pi)
        adapter.save(
            createStatement(subject = si, predicate = pi, `object` = s).copy(id = StatementId(1000))
        )

        // Verify that all changes are accounted for:
        assertThat(adapter.findAll(PageRequest.of(0, 10)).totalElements).isEqualTo(5 + 1)
        val actual = adapter.findByStatementId(modifiedId).get()
        // Again, this should not be needed… see above.
        val expected = modified.copy(subject = modified.subject)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    @Tag("regression")
    @Disabled("This will add minutes to the test times, and does not trigger the problem. :-/")
    fun `saving and retrieving a lot of statements with the same predicate`() {
        val predicate = createPredicate().copy(id = PredicateId("P30"))
        predicateRepository.save(predicate)
        val resources = (1L..50).map { createResource().copy(id = ResourceId(it), label = randomString()) }
        resources.forEach { resourceRepository.save(it) }
        // Create random statements with the same predicate
        val statements = (1L..2_000 + 100).map {
            createStatement(
                subject = resources[Random.nextInt(0, resources.size)],
                predicate = predicate,
                `object` = resources[Random.nextInt(0, resources.size)],
            ).copy(id = StatementId(it))
        }
        statements.forEach { statement: GeneralStatement ->
            adapter.save(statement)
        }

        val found = adapter.findAll(PageRequest.of(0, 2_000))

        assertThat(found).hasSize(2_000)
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}

internal fun randomString(length: Int = 20, charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')) =
    (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
