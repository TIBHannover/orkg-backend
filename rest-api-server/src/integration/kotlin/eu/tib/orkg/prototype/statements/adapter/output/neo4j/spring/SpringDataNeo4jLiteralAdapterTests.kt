package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createLiteral
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SpringDataNeo4jLiteralAdapterTests : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jLiteralAdapter

    @Test
    fun `saving, loading and saving modified works`() {
        val literal = createLiteral()
        adapter.save(literal)
        val found = adapter.findByLiteralId(literal.id).get()
        val modified = found.copy(label = "some new label, never seen before")
        adapter.save(modified)

        assertThat(adapter.findAll().toSet().size).isEqualTo(1)
        assertThat(adapter.findByLiteralId(literal.id).get().label)
            .isEqualTo("some new label, never seen before")
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
