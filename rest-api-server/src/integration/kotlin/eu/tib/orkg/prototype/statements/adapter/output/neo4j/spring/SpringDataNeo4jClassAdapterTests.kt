package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SpringDataNeo4jClassAdapterTests : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jClassAdapter

    @Test
    fun `saving, loading and saving modified works`() {
        val `class` = createClass()
        adapter.save(`class`)
        val found = adapter.findByClassId(`class`.id).get()
        val modified = found.copy(label = "some new label, never seen before")
        adapter.save(modified)

        assertThat(adapter.findAll().toSet().size).isEqualTo(1)
        assertThat(adapter.findByClassId(`class`.id).get().label)
            .isEqualTo("some new label, never seen before")
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
