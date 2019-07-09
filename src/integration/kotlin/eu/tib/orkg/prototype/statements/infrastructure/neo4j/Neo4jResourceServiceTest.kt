package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
@DisplayName("Neo4: Resource service")
class Neo4jResourceServiceTest {

    @Autowired
    private lateinit var service: ResourceService

    @Test
    @DisplayName("should create resource from request")
    fun shouldCreateResourceFromRequest() {
        val resource = service.create(CreateResourceRequest(ResourceId("someID"), "Some Concept"))

        assertThat(resource.id.toString()).isEqualTo("someID")
        assertThat(resource.label).isEqualTo("Some Concept")
    }

    @Test
    @DisplayName("should find created resources")
    fun shouldFindCreatedResources() {
        service.create("first")
        service.create("second")

        val resources = service.findAll()
        val labels = resources.map(Resource::label)

        assertThat(resources).hasSize(2)
        assertThat(labels).containsExactlyInAnyOrder("first", "second")
    }

    @Test
    @DisplayName("should return an empty list when label was not found")
    fun shouldReturnEmptyListWhenNotFound() {
        service.create("first")
        service.create("second")

        val result = service.findAllByLabel("not in the list")

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("should return all resource that match the label (duplicates allowed)")
    fun shouldReturnSeveralWhenSame() {
        service.create("same")
        service.create("same")
        service.create("other")
        service.create("yet another")

        val result = service.findAllByLabel("same")

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return resource containing substring")
    fun shouldNotReturnResourceContainingSubstring() {
        service.create("this is part of the test")
        assertThat(service.findAllByLabel("part")).isEmpty()
    }

    @Test
    @DisplayName("when matching label partially should return all occurrences")
    fun whenMatchingLabelPartiallyShouldReturnAllOccurrences() {
        service.create("first part is this")
        service.create("this is another part")
        service.create("part at the beginning")
        service.create("something else")

        val result = service.findAllByLabelContaining("part")

        assertThat(result).hasSize(3)
    }

    @Test
    @DisplayName("should return resource with the same ID")
    fun shouldReturnResourceWithTheSameId() {
        service.create("irrelevant")
        service.create("also irrelevant")
        val expectedId = service.create("to be found").id
        println("expected: $expectedId")
        val found = service.findById(expectedId)
        println("found: $found")
        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(expectedId)
    }
}
