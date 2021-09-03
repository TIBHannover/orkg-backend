package eu.tib.orkg.prototype.statements.domain.model.neo4j

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.data.neo4j.core.Neo4jClient

@DataNeo4jTest
class Neo4jResourceTest(
    neo4jClient: Neo4jClient
) : Neo4jTestContainersBaseTest(neo4jClient) {

    @Autowired
    private lateinit var repository: Neo4jResourceRepository

    @BeforeEach
    fun setup() {
        repository.deleteAll()

        assertThat(repository.findAll()).hasSize(0)
    }

    @Test
    @DisplayName("When no labels are provided Then the list should be empty")
    fun whenNoLabelsAreProvidedThenTheListShouldBeEmpty() {
        val resource = Neo4jResource("irrelevant", ResourceId("irrelevant"))

        val persistedResource = repository.save(resource)

        assertThat(persistedResource.classes).isEmpty()
    }

    @Test
    @DisplayName("When labels are provided Then they should be persisted")
    fun whenLabelsAreProvidedThenTheyShouldBePersisted() {
        val resource = Neo4jResource("irrelevant", ResourceId("irrelevant"))
        resource.assignTo("Foo")
        resource.assignTo("Bar")

        val persistedResource = repository.save(resource)

        assertThat(persistedResource.classes).containsExactlyInAnyOrder(ClassId("Foo"), ClassId("Bar"))
    }
}
