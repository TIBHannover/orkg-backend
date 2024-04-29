package org.orkg.graph.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.createPredicate
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest

@Neo4jContainerIntegrationTest
class PredicateServiceIntegrationTest {

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
        service.createPredicate(label = "third")
        service.createPredicate(label = "fourth")

        assertThat(service.findAll(PageRequest.of(0, 10))).hasSize(2)

        val result =
            service.findAllByLabel(SearchString.of("not in the list", exactMatch = true), PageRequest.of(0, 10))

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("should return all predicates that match the label (duplicates allowed)")
    fun shouldReturnAllPredicatesThatMatchTheLabelDuplicatesAllowed() {
        service.createPredicate(label = "same")
        service.createPredicate(label = "same")
        service.createPredicate(label = "other")
        service.createPredicate(label = "yet another")

        val result = service.findAllByLabel(SearchString.of("same", exactMatch = true), PageRequest.of(0, 10))

        assertThat(result).hasSize(2)
    }

    @Test
    @DisplayName("should not return predicate containing substring")
    fun shouldNotReturnPredicateContainingSubstring() {
        service.createPredicate(label = "this is part of the test")
        assertThat(service.findAllByLabel(SearchString.of("part", exactMatch = true), PageRequest.of(0, 10))).isEmpty()
    }

    @Test
    @DisplayName("when matching label partially should return all occurrences")
    fun whenMatchingLabelPartiallyShouldReturnAllOccurrences() {
        service.createPredicate(label = "first part is this")
        service.createPredicate(label = "this is another part")
        service.createPredicate(label = "part at the beginning")
        service.createPredicate(label = "something else")

        val result = service.findAllByLabel(SearchString.of("part", exactMatch = false), PageRequest.of(0, 10))

        assertThat(result).hasSize(3)
    }

    @Test
    @DisplayName("should return resource with the same ID")
    fun shouldReturnResourceWithTheSameId() {
        service.createPredicate(label = "irrelevant")
        service.createPredicate(label = "also irrelevant")
        val expectedId = service.createPredicate(label = "to be found")

        val found = service.findById(expectedId)

        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(expectedId)
    }
}
