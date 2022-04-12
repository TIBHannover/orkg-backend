package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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

        // Create an "incoming" statement for the subject and validate "shared" property.
        // It is important to do it after the modification to see if other changes are picked up properly.
        // TODO: This should be part of the *representation*, not the domain!
        val si = createResource().copy(id = ResourceId(1000))
        resourceRepository.save(si)
        val pi = createPredicate().copy(id = PredicateId(1000), label = "incoming")
        predicateRepository.save(pi)
        adapter.save(
            createStatement(subject = si, predicate = pi, `object` = s).copy(id = StatementId(1000))
        )
        assertThat(resourceRepository.findByResourceId(s.id).get().shared).isEqualTo(1)

        // Verify that all changes are accounted for:
        assertThat(adapter.findAll(PageRequest.of(0, 10)).totalElements).isEqualTo(5 + 1)
        val actual = adapter.findByStatementId(modifiedId).get()
        // Again, this should not be needed… see above.
        val expected = modified.copy(subject = (modified.subject as Resource).copy(shared = 1))
        assertThat(actual).isEqualTo(expected)
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
