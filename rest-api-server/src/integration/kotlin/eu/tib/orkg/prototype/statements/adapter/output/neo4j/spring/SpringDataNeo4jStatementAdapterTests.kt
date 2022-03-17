package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.createStatement
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
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
    @Disabled("There seems to be an issue with transaction handling/flushing. It works OK in the running app. Not sure what the issue is, except me being an idiot. -- MP")
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
        val expected = adapter.findByStatementId(statement.id!!).get()
        assertThat(expected).isEqualTo(modified)
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
