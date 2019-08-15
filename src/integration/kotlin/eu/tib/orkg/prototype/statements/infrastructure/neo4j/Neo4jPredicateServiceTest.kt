package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.Neo4jServiceTest
import eu.tib.orkg.prototype.statements.application.CreatePredicateRequest
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@Neo4jServiceTest
@DisplayName("Neo4j: Predicate service")
class Neo4jPredicateServiceTest {

    @Autowired
    private lateinit var service: PredicateService

    @Test
    @DisplayName("should create predicate from request")
    fun shouldCreatePredicateFromRequest() {
        val resource = service.create(CreatePredicateRequest(PredicateId("someID"), "Some Concept"))

        assertThat(resource.id.toString()).isEqualTo("someID")
        assertThat(resource.label).isEqualTo("Some Concept")
    }

    @Test
    @DisplayName("should find created predicates")
    fun shouldFindCreatedPredicates() {
        service.create("first")
        service.create("second")

        val predicates = service.findAll()
        val labels = predicates.map(Predicate::label)

        assertThat(predicates).hasSize(2)
        assertThat(labels).containsExactlyInAnyOrder("first", "second")
    }

    @Test
    @DisplayName("should return empty list when label was not found")
    fun shouldReturnEmptyListWhenLabelWasNotFound() {
        service.create("first")
        service.create("second")

        assertThat(service.findAll()).hasSize(2)

        val result = service.findAllByLabel("not in the list")

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("should return all predicates that match the label (duplicates allowed)")
    fun shouldReturnAllPredicatesThatMatchTheLabelDuplicatesAllowed() {
        service.create("same")
        service.create("same")
        service.create("other")
        service.create("yet another")

        val result = service.findAllByLabel("same")

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return predicate containing substring")
    fun shouldNotReturnPredicateContainingSubstring() {
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

        val found = service.findById(expectedId)

        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(expectedId)
    }
}
