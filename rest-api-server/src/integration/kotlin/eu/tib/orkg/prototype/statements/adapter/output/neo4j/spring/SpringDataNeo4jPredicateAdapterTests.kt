package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class SpringDataNeo4jPredicateAdapterTests : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jPredicateAdapter

    @Test
    fun `saving, loading and saving modified works`() {
        val predicate = createPredicate()
        adapter.save(predicate)
        val found = adapter.findByPredicateId(predicate.id).get()
        val modified = found.copy(label = "some new label, never seen before")
        adapter.save(modified)

        assertThat(adapter.findAll(PageRequest.of(0, 10)).totalElements).isEqualTo(1)
        assertThat(adapter.findByPredicateId(predicate.id).get().label)
            .isEqualTo("some new label, never seen before")
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
