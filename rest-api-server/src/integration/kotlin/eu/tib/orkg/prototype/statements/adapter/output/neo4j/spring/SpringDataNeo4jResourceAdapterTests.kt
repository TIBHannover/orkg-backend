package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class SpringDataNeo4jResourceAdapterTests : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var adapter: SpringDataNeo4jResourceAdapter

    @Test
    fun `saving, loading and saving modified works`() {
        val resource = createResource()
        adapter.save(resource)
        val found = adapter.findByResourceId(resource.id).get()
        val modified = found.copy(label = "some new label, never seen before")
        adapter.save(modified)

        assertThat(adapter.findAll(PageRequest.of(0, 10)).toSet().size).isEqualTo(1)
        assertThat(adapter.findByResourceId(resource.id).get().label).isEqualTo("some new label, never seen before")
    }

    @AfterEach
    fun workaroundToResetDatabase() {
        adapter.deleteAll()
    }
}
