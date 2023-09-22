package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.testing.Neo4jTestContainersBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

class PredicateServiceIntegrationTest : Neo4jTestContainersBaseTest() {

    @Autowired
    private lateinit var service: PredicateService

    @BeforeEach
    fun setup() {
        service.removeAll()

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(0)
    }

    @Test
    @DisplayName("should return empty list when label was not found")
    fun shouldReturnEmptyListWhenLabelWasNotFound() {
        service.create("third")
        service.create("fourth")

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(2)

        val result =
            service.findAllByLabel(SearchString.of("not in the list", exactMatch = true), PageRequest.of(0, 10))

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("should return all predicates that match the label (duplicates allowed)")
    fun shouldReturnAllPredicatesThatMatchTheLabelDuplicatesAllowed() {
        service.create("same")
        service.create("same")
        service.create("other")
        service.create("yet another")

        val result = service.findAllByLabel(SearchString.of("same", exactMatch = true), PageRequest.of(0, 10))

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return predicate containing substring")
    fun shouldNotReturnPredicateContainingSubstring() {
        service.create("this is part of the test")
        assertThat(service.findAllByLabel(SearchString.of("part", exactMatch = true), PageRequest.of(0, 10))).isEmpty()
    }

    @Test
    @DisplayName("when matching label partially should return all occurrences")
    fun whenMatchingLabelPartiallyShouldReturnAllOccurrences() {
        service.create("first part is this")
        service.create("this is another part")
        service.create("part at the beginning")
        service.create("something else")

        val result = service.findAllByLabel(SearchString.of("part", exactMatch = false), PageRequest.of(0, 10))

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
