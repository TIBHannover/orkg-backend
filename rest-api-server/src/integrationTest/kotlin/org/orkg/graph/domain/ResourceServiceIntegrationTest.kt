package org.orkg.graph.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@Neo4jContainerIntegrationTest
class ResourceServiceIntegrationTest {

    @Autowired
    private lateinit var service: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @BeforeEach
    fun setup() {
        service.removeAll()
        classService.removeAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    @DisplayName("should create resource from request")
    fun shouldCreateResourceFromRequest() {
        val resource = service.create(
            CreateResourceUseCase.CreateCommand(id = ThingId("someID"), label = "Some Concept")
        )

        assertThat(resource.value).isEqualTo("someID")
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched exactly, Then it should return the resource")
    fun whenSearchedExactlyThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabel(
            label = SearchString.of(label, exactMatch = true),
            pageable = PageRequest.of(0, 10)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched exactly but surrounded by extra whitespace, Then it should return the resource")
    fun whenSearchedExactlyButSurroundedByExtraWhitespaceThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabel(
            label = SearchString.of("  $label\t ", exactMatch = true),
            pageable = PageRequest.of(0, 10)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched partially, Then it should return the resource")
    fun whenSearchedPartiallyThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabel(
            label = SearchString.of("label with", exactMatch = false),
            pageable = PageRequest.of(0, 10)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label that contains whitespaces, When searched with extra whitespace in the search string, Then it should return the resource")
    fun whenSearchedWithExtraWhitespaceInTheSearchStringThenItShouldReturnTheResource() {
        val label = "a label with whitespace"
        service.create(label)

        val result = service.findAllByLabel(
            label = SearchString.of("label \t  with", exactMatch = false),
            pageable = PageRequest.of(0, 10)
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().label).isEqualTo(label)
    }

    @Test
    @DisplayName("Given a resource with a label containing multiple consecutive whitespace, When searched with single whitespace, Then it should return the resource")
    fun whenSearchedWithSingleWhitespaceThenItShouldReturnTheResource() {
        val label = "one two  three   four"
        service.create(label)

        val result = service.findAllByLabel(
            label = SearchString.of("one two three four", exactMatch = false),
            pageable = PageRequest.of(0, 10)
        )

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

        val result = service.findAllByLabel(
            label = SearchString.of("not in the list", exactMatch = true),
            pageable = pagination
        )

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

        val result = service.findAllByLabel(
            label = SearchString.of("same", exactMatch = true),
            pageable = pagination
        )

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return resource containing substring")
    fun shouldNotReturnResourceContainingSubstring() {
        val pagination = PageRequest.of(0, 10)
        service.create("this is part of the test")
        assertThat(service.findAllByLabel(SearchString.of("part", exactMatch = true), pagination)).isEmpty()
    }

    @Test
    @DisplayName("when matching label partially should return all occurrences")
    fun whenMatchingLabelPartiallyShouldReturnAllOccurrences() {
        service.create("first part is this")
        service.create("this is another part")
        service.create("part at the beginning")
        service.create("something else")
        val pagination = PageRequest.of(0, 10)
        val result = service.findAllByLabel(
            label = SearchString.of("part", exactMatch = false),
            pageable = pagination
        )

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
        val res = service.create("C\$razy LAb(el. he*r?").id
        val found = service.findAllByLabel(
            label = SearchString.of("LAb(el.", exactMatch = false),
            pageable = PageRequest.of(1, 10)
        )
        assertThat(found).isNotNull
        assertThat(found.map(Resource::id).contains(res))
    }

    @Test
    fun `when several resources of a class exist with the same label, partial search should return all of them`() {
        val researchProblemClass = classService.create("ResearchProblem").id
        val resources = mutableListOf<Resource>()
        repeat(5) {
            resources += service.create(
                CreateResourceUseCase.CreateCommand(
                    label = "Testing the Darwin's naturalisation hypothesis in invasion biology",
                    classes = setOf(researchProblemClass),
                )
            ).let { service.findById(it).get() }
        }
        assertThat(service.findAll(PageRequest.of(0, 10_000)).totalElements).isEqualTo(5)

        val page = PageRequest.of(0, 10)
        val found = service.findAllByClassAndLabel(
            id = researchProblemClass,
            label = SearchString.of("Testing the Darwin", exactMatch = false),
            pageable = page
        ).map(Resource::id)

        assertThat(found.totalElements).isEqualTo(5)
        assertThat(found.content).containsExactlyInAnyOrderElementsOf(resources.map(Resource::id))
    }
}
