package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.application.CreateResourceRequest
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest

@SpringBootTest
@DisplayName("Neo4: Resource service")
class Neo4jResourceServiceTest {

    @Autowired
    private lateinit var service: ResourceService

    @BeforeEach
    fun setup() {
        service.removeAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    @DisplayName("should create resource from request")
    fun shouldCreateResourceFromRequest() {
        val resource = service.create(CreateResourceRequest(ResourceId("someID"), "Some Concept"))

        assertThat(resource.id.toString()).isEqualTo("someID")
        assertThat(resource.label).isEqualTo("Some Concept")
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched exactly, Then it should return the resource")
    fun whenSearchedExactlyThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabelContaining(PageRequest.of(0, 10), label)

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched exactly but surrounded by extra whitespace, Then it should return the resource")
    fun whenSearchedExactlyButSurroundedByExtraWhitespaceThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabelContaining(PageRequest.of(0, 10), "  $label\t ")

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched partially, Then it should return the resource")
    fun whenSearchedPartiallyThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabelContaining(PageRequest.of(0, 10), "bel wi")

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched with extra whitespace in the search string, Then it should return the resource")
    fun whenSearchedWithExtraWhitespaceInTheSearchStringThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabelContaining(PageRequest.of(0, 10), "label \t  with")

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label containing multiple consecutive whitespace, When searched with single whitespace, Then it should return the resource")
    fun whenSearchedWithSingleWhitespaceThenItShouldReturnTheResource() {
        val label = "one two  three   four"
        service.create(label)

        val result = service.findAllByLabelContaining(PageRequest.of(0, 10), "one two three four")

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("should find created resources")
    fun shouldFindCreatedResources() {
        service.create("first")
        service.create("second")
        val pagination = PageRequest.of(0, 10)
        val resources = service.findAll(pagination)
        val labels = resources.map(Resource::label)

        assertThat(resources).hasSize(2)
        assertThat(labels).containsExactlyInAnyOrder("first", "second")
    }

    @Test
    @DisplayName("should return an empty list when label was not found")
    fun shouldReturnEmptyListWhenNotFound() {
        val pagination = PageRequest.of(0, 10)
        service.create("first")
        service.create("second")

        val result = service.findAllByLabel(pagination, "not in the list")

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("should return all resource that match the label (duplicates allowed)")
    fun shouldReturnSeveralWhenSame() {
        service.create("same")
        service.create("same")
        service.create("other")
        service.create("yet another")
        val pagination = PageRequest.of(0, 10)

        val result = service.findAllByLabel(pagination, "same")

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return resource containing substring")
    fun shouldNotReturnResourceContainingSubstring() {
        val pagination = PageRequest.of(0, 10)
        service.create("this is part of the test")
        assertThat(service.findAllByLabel(pagination, "part")).isEmpty()
    }

    @Test
    @DisplayName("when matching label partially should return all occurrences")
    fun whenMatchingLabelPartiallyShouldReturnAllOccurrences() {
        service.create("first part is this")
        service.create("this is another part")
        service.create("part at the beginning")
        service.create("something else")
        val pagination = PageRequest.of(0, 10)
        val result = service.findAllByLabelContaining(pagination, "part")

        assertThat(result).hasSize(3)
    }

    @Test
    @DisplayName("should return resource with the same ID")
    fun shouldReturnResourceWithTheSameId() {
        service.create("irrelevant")
        service.create("also irrelevant")
        val expectedId = service.create("to be found").id
        val found = service.findById(expectedId)
        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(expectedId)
    }

    @Test
    @DisplayName("should allow regex special chars in resource label")
    fun shouldAllowRegexSpecialCharsInLabel() {
        val res = service.create("C\$razy LAb(el. he*r?")
        val found = service.findAllByLabelContaining(
            PageRequest.of(1, 10), "LAb(el."
        )
        assertThat(found).isNotNull
        assertThat(found.contains(res))
    }
}
