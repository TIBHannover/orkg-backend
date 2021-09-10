package eu.tib.orkg.prototype.statements.ports

import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable

interface ResourceRepositoryContract {
    val repository: ResourceRepository

    @Test
    @DisplayName("creates a node when saving")
    fun createsNodeWhenSaving() {
        assertThat(repository.count()).isEqualTo(0L)
        repository.save(Resource(ResourceId("R1"), "some resource"))
        assertThat(repository.count()).isEqualTo(1L)
    }

    @Test
    @DisplayName("finds an existing resource by its ID")
    fun findsById() {
        repository.save(Resource(ResourceId("Vievelaim8Ee"), label = "pae7aeWoonai"))
        val actual = repository.findById(ResourceId("Vievelaim8Ee")).get()
        assertThat(actual.id).isEqualTo(ResourceId("Vievelaim8Ee"))
        assertThat(actual.label).isEqualTo("pae7aeWoonai")
    }

    @Test
    @DisplayName("produces new, consecutive IDs")
    fun producesNewIds() {
        val numberOfIds = 10
        val expected = (0L until numberOfIds).toList().map(::ResourceId)
        val producedIds = mutableListOf<ResourceId>()
        repeat(numberOfIds) {
            producedIds += repository.nextIdentity()
        }
        assertThat(producedIds).isEqualTo(expected)
    }

    @Test
    @DisplayName("finds parts of the label if label contains regular words")
    fun matchesLabelFuzzyRegularWords() {
        repository.save(Resource(repository.nextIdentity(), "research contribution"))
        repository.save(Resource(repository.nextIdentity(), "research topic"))
        repository.save(Resource(repository.nextIdentity(), "programming language"))

        val pagedResult = repository.findAllByLabelContaining("research", Pageable.unpaged())

        assertThat(pagedResult.totalElements).isEqualTo(2)
    }

    @Test
    @DisplayName("finds parts of the label if label contains special characters (parenthesis)")
    fun matchesLabelFuzzyWithSpecialChars() {
        repository.save(Resource(repository.nextIdentity(), "research contribution"))
        repository.save(Resource(repository.nextIdentity(), "research topic"))
        val expected = Resource(repository.nextIdentity(), "programming language (PL)").also {
            repository.save(it)
        }

        val pagedResult = repository.findAllByLabelContaining("PL", Pageable.unpaged())

        assertThat(pagedResult.totalElements).isEqualTo(1)
        assertThat(pagedResult.content.first().id).isEqualTo(expected.id)
        assertThat(pagedResult.content.first().label).isEqualTo(expected.label)
    }
}
